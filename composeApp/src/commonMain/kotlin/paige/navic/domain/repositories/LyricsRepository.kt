package paige.navic.domain.repositories

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.entities.LyricEntity
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.lyrics.LyricsConfig
import paige.navic.domain.models.lyrics.LyricsLine
import paige.navic.domain.models.lyrics.LyricsProvider
import paige.navic.domain.models.lyrics.LyricsResult
import paige.navic.domain.parser.LyricsContentParser
import paige.navic.util.core.Logger
import kotlin.time.Duration.Companion.milliseconds

class LyricsRepository(
	private val lyricDao: LyricDao,
	private val settings: Settings,
	private val sessionManager: SessionManager
) {

	private val client = HttpClient {
		install(HttpTimeout) {
			requestTimeoutMillis = 40000
			connectTimeoutMillis = 40000
			socketTimeoutMillis = 40000
		}
	}
	private val json = Json { ignoreUnknownKeys = true }

	// regex for finding parenthesis as they are often for crediting producers which can make lrclib
	// miss a search. example "songTitle (prod. producer123)"
	private companion object {
		val PARENTHETICAL = Regex("""\([^)]*\)""")
		val REPEATED_WHITESPACE = Regex("""\s+""")
	}

	private fun getConfig(): LyricsConfig {
		val raw = settings.getStringOrNull(LyricsConfig.KEY)
		return try {
			if (raw != null) json.decodeFromString<LyricsConfig>(raw)
			else LyricsConfig()
		} catch (_: Exception) {
			LyricsConfig()
		}
	}

	suspend fun fetchLyrics(song: DomainSong): LyricsResult? {
		try {
			val cached = lyricDao.getLyrics(song.id)
			if (cached != null) {
				val parsed = LyricsContentParser.parse(cached.rawContent)
				if (!parsed.isNullOrEmpty()) return LyricsResult(
					parsed,
					cached.provider,
					cached.rawContent
				)
			}
		} catch (ex: Exception) {
			Logger.w("LyricsRepository", "failed getting cached lyrics", ex)
		}

		val currentConfig = getConfig()
		for (provider in currentConfig.priority) {
			try {
				var rawContentToCache: String? = null

				val parsedLyrics = when (provider) {
					LyricsProvider.LYRICS_PLUS -> {
						val raw = fetchRawLyricsPlus(song, currentConfig)
						rawContentToCache = raw
						raw?.let { LyricsContentParser.parse(it) }
					}

					LyricsProvider.LRCLIB -> {
						val raw = fetchRawLrcLib(song, currentConfig)
						rawContentToCache = raw
						raw?.let { LyricsContentParser.parse(it) }
					}

					LyricsProvider.SUBSONIC -> {
						val subsonicLyrics = sessionManager.api.getLyrics(song.id).firstOrNull()

						val lines = subsonicLyrics?.lines?.flatMap { line ->
							if (!subsonicLyrics.synced && line.value.contains("\n")) {
								line.value.lineSequence()
									.filter { it.isNotBlank() }
									.map { LyricsLine(time = null, text = it.trim()) }
									.toList()
							} else {
								val time =
									if (subsonicLyrics.synced) line.start?.milliseconds else null
								listOf(LyricsLine(time = time, text = line.value))
							}
						}

						if (!lines.isNullOrEmpty()) {
							rawContentToCache = lines.joinToString("\n") { l ->
								val t = l.time
								if (t != null) {
									val m = t.inWholeMinutes.toString().padStart(2, '0')
									val s = (t.inWholeSeconds % 60).toString().padStart(2, '0')
									val ms = ((t.inWholeMilliseconds % 1000) / 10).toString()
										.padStart(2, '0')
									"[$m:$s.$ms]${l.text}"
								} else l.text
							}
						}
						lines
					}
				}

				if (!parsedLyrics.isNullOrEmpty()) {
					try {
						rawContentToCache?.let { content ->
							val entity = LyricEntity(
								songId = song.id,
								provider = provider,
								rawContent = content
							)
							lyricDao.insertLyrics(entity)
						}
					} catch (e: Exception) {
						Logger.e("LyricRepository", "Failed to cache lyrics for ${song.title}", e)
					}
					return LyricsResult(parsedLyrics, provider, rawContentToCache)
				}
			} catch (e: Exception) {
				Logger.e("LyricRepository", "Provider ${provider.name} failed!", e)
				continue
			}
		}
		return null
	}

	private suspend fun fetchRawLrcLib(song: DomainSong, config: LyricsConfig): String? {
		return try {
			val response = client.get(config.lrcLibBaseUrl) {
				// using /api/search with q param to search for lyrics more loosely, finds lyrics
				// more consistently because of how one may tag their music. Also looking at how i.e
				// navidrome tags albumless songs with [Unknown Album] which will never find a result
				parameter("q", "${song.title.withoutParentheticals()} ${song.artistName}")
				accept(ContentType.Application.Json)
			}
			if (!response.status.isSuccess()) {
				throw Exception("unsuccessful status code ${response.status.value}")
			}

			// lrclib returns up to 20 results in the form of a json object, select the first one
			// with synced lyrics, or first one with unsynced lyrics if none have any synced. Better
			// matching can be added further to then look for songs that do have a similar album or
			// duration though first result should be fine?
			val results = json.parseToJsonElement(response.bodyAsText()).jsonArray
			val match = results.firstOrNull { it.lyricsOrNull("syncedLyrics") != null }
				?: results.firstOrNull { it.lyricsOrNull("plainLyrics") != null }

			match?.toString()
		} catch (ex: Exception) {
			Logger.w(
				"LyricsRepository",
				"failed fetching from lrclib",
				ex
			)
			null
		}
	}

	private fun JsonElement.lyricsOrNull(key: String): String? =
		jsonObject[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

	// remove parts of song title as defined by regex PARENTHETICAL
	private fun String.withoutParentheticals(): String =
		replace(PARENTHETICAL, " ")
			.replace(REPEATED_WHITESPACE, " ")
			.trim()
			.ifEmpty { this }

	private suspend fun fetchRawLyricsPlus(song: DomainSong, config: LyricsConfig): String? =
		coroutineScope {
			val resultChannel = Channel<String>(Channel.UNLIMITED)
			val jobs = config.lyricsPlusMirrors.map { baseUrl ->
				launch {
					try {
						val response = client.get("$baseUrl/v2/lyrics/get") {
							parameter("title", song.title)
							parameter("artist", song.artistName)
							parameter("album", song.albumTitle)
							parameter("duration", song.duration)
							accept(ContentType.Application.Json)
						}
						if (response.status.isSuccess()) {
							resultChannel.send(response.bodyAsText())
						} else {
							throw Exception("unsuccessful status code ${response.status.value}")
						}
					} catch (ex: Exception) {
						Logger.w("LyricsRepository", "failed fetching from provider $baseUrl", ex)
					}
				}
			}

			launch {
				jobs.joinAll()
				resultChannel.close()
			}

			val result = resultChannel.receiveCatching().getOrNull()
			jobs.forEach { it.cancel() }
			result
		}
}

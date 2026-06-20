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
import kotlinx.serialization.json.Json
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
		} catch (_: Exception) {
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
								val time = if (subsonicLyrics.synced) line.start?.milliseconds else null
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
				parameter("track_name", song.title)
				parameter("artist_name", song.artistName)
				parameter("album_name", song.albumTitle)
				parameter("duration", song.duration)
				accept(ContentType.Application.Json)
			}
			if (response.status.isSuccess()) response.bodyAsText() else null
		} catch (_: Exception) {
			null
		}
	}

	private suspend fun fetchRawLyricsPlus(song: DomainSong, config: LyricsConfig): String? {
		for (baseUrl in config.lyricsPlusMirrors) {
			try {
				val response = client.get("$baseUrl/v2/lyrics/get") {
					parameter("title", song.title)
					parameter("artist", song.artistName)
					parameter("album", song.albumTitle)
					parameter("duration", song.duration)
					accept(ContentType.Application.Json)
				}
				if (response.status.isSuccess()) {
					return response.bodyAsText()
				}
			} catch (_: Exception) {
				continue
			}
		}
		return null
	}
}

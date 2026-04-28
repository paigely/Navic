package paige.navic.domain.repositories

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.shared.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

data class LyricWord(val time: Duration, val duration: Duration, val text: String)

data class LyricLine(
	val time: Duration? = null,
	val text: String,
	val words: List<LyricWord>? = null
)

data class LyricsResult(
	val lines: List<LyricLine>,
	val provider: LyricsProvider,
	val rawContent: String? = null
) {
	val isSynced: Boolean = lines.any { it.time != null }
}

@Serializable
enum class LyricsProvider(
	val displayName: String
) {
	LYRICS_PLUS("YouLy+"),
	SUBSONIC("Subsonic"),
	LRCLIB("Lrclib")
}

@Serializable
data class LyricsConfig(
	val priority: List<LyricsProvider> = listOf(
		LyricsProvider.LYRICS_PLUS,
		LyricsProvider.SUBSONIC,
		LyricsProvider.LRCLIB
	),
	val lyricsPlusMirrors: List<String> = listOf(
		"https://lyricsplus.atomix.one",
		"https://lyricsplus-seven.vercel.app",
		"https://lyricsplus.prjktla.workers.dev"
	),
	val lrcLibBaseUrl: String = "https://lrclib.net/api/get"
) {
	companion object {
		const val KEY = "lyrics_config_prefs"
	}
}

@Serializable
private data class YoulyResponse(
	val lyrics: List<YoulyLine> = emptyList()
)

@Serializable
private data class YoulyLine(
	val time: Long = 0L,
	val text: String = "",
	val syllabus: List<YoulySyllable>? = null
)

@Serializable
private data class YoulySyllable(
	val time: Long = 0L,
	val duration: Long = 0L,
	val text: String = ""
)

object LyricsContentParser {
	private val jsonParser = Json {
		isLenient = true
		explicitNulls = false
		ignoreUnknownKeys = true
	}

	fun parse(content: String): List<LyricLine>? {
		val text = content.trim()
		if (text.isEmpty()) return null

		return try {
			if (text.startsWith("{")) {
				parseJson(text)
			} else {
				parseLrc(text)
			}
		} catch (e: Exception) {
			Logger.e("LyricRepository", "Lyrics parsing failed!", e)
			null
		}
	}

	private fun parseJson(jsonString: String): List<LyricLine>? {
		val jsonObject = jsonParser.parseToJsonElement(jsonString).jsonObject

		val syncedStr = jsonObject["syncedLyrics"]?.jsonPrimitive?.contentOrNull
		if (!syncedStr.isNullOrEmpty()) {
			return parseLrc(syncedStr)
		}

		val plainStr = jsonObject["plainLyrics"]?.jsonPrimitive?.contentOrNull
		if (!plainStr.isNullOrEmpty()) {
			return plainStr.lineSequence()
				.map { LyricLine(text = it.trim()) }
				.toList()
		}

		if (jsonObject.containsKey("lyrics")) {
			val youlyResponse = jsonParser.decodeFromString<YoulyResponse>(jsonString)
			return parseYoulyResponse(youlyResponse)
		}

		return null
	}

	private fun parseYoulyResponse(response: YoulyResponse): List<LyricLine>? {
		if (response.lyrics.isEmpty()) return null
		return response.lyrics.map { line ->
			LyricLine(
				time = line.time.milliseconds,
				text = line.text,
				words = line.syllabus?.map { syl ->
					LyricWord(syl.time.milliseconds, syl.duration.milliseconds, syl.text)
				}
			)
		}.sortedBy { it.time }
	}

	private fun parseLrc(input: String): List<LyricLine> {
		val lines = input.lineSequence().toList()

		if (!input.contains("[")) {
			return lines.map { LyricLine(text = it.trim()) }
		}

		return lines
			.filter { it.isNotBlank() }
			.mapNotNull { line ->
				try {
					if (line.startsWith("[") && line.contains("]")) {
						val close = line.indexOf(']')
						val timestamp = line.substring(1, close)
						val text = line.substring(close + 1).trim()

						if (!timestamp.contains(':') || timestamp.any { it.isLetter() }) {
							return@mapNotNull if (text.isNotEmpty()) LyricLine(text = text) else null
						}

						val parts = timestamp.split(':', '.')
						val minutes = parts[0].toLong()
						val seconds = parts[1].toLong()
						val hundredths = parts.getOrNull(2)?.toLong() ?: 0L
						val duration =
							minutes.minutes + seconds.seconds + (hundredths * 10).milliseconds

						LyricLine(time = duration, text = text)
					} else {
						LyricLine(text = line.trim())
					}
				} catch (_: Exception) {
					null
				}
			}
			.toList()
			.sortedBy { it.time }
	}
}

class LyricRepository(
	private val lyricDao: LyricDao,
	private val client: HttpClient,
	private val settings: Settings
) {

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
						val subsonicLyrics = SessionManager.api.getLyrics(song.id).firstOrNull()

						val lines = subsonicLyrics?.lines?.flatMap { line ->
							if (!subsonicLyrics.synced && line.value.contains("\n")) {
								line.value.lineSequence()
									.filter { it.isNotBlank() }
									.map { LyricLine(time = null, text = it.trim()) }
									.toList()
							} else {
								val time = if (subsonicLyrics.synced) line.start.milliseconds else null
								listOf(LyricLine(time = time, text = line.value))
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

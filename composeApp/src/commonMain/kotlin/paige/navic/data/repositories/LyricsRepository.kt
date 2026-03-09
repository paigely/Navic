package paige.navic.data.repositories

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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.compose.resources.StringResource
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track
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
	val provider: LyricsProvider
)

@Serializable
enum class LyricsProvider(
	val displayName: String
) {
	LYRICS_PLUS("Lyrics Plus"),
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
			println("Lyrics parsing failed: ${e.message}")
			null
		}
	}

	private fun parseJson(jsonString: String): List<LyricLine>? {
		val jsonObject = jsonParser.parseToJsonElement(jsonString).jsonObject

		return when {
			jsonObject.containsKey("subsonic-response") -> {
				parseSubsonicJson(jsonObject)
			}
			jsonObject.containsKey("syncedLyrics") -> {
				val syncedStr = jsonObject["syncedLyrics"]?.jsonPrimitive?.contentOrNull
				if (!syncedStr.isNullOrEmpty()) parseLrc(syncedStr) else null
			}
			jsonObject.containsKey("lyrics") -> {
				val youlyResponse = jsonParser.decodeFromString<YoulyResponse>(jsonString)
				parseYoulyResponse(youlyResponse)
			}
			else -> null
		}
	}

	private fun parseSubsonicJson(root: JsonObject): List<LyricLine>? {
		val response = root["subsonic-response"]?.jsonObject ?: return null

		if (response["status"]?.jsonPrimitive?.contentOrNull == "failed") return null

		val structuredLyrics = response["lyricsList"]?.jsonObject?.get("structuredLyrics")?.jsonArray
		val syncedLyrics = structuredLyrics?.firstOrNull {
			it.jsonObject["synced"]?.jsonPrimitive?.booleanOrNull == true
		} ?: structuredLyrics?.firstOrNull()

		return syncedLyrics?.jsonObject?.get("line")?.jsonArray?.mapNotNull { line ->
			val startMs = line.jsonObject["start"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
			val value = line.jsonObject["value"]?.jsonPrimitive?.contentOrNull
			if (startMs != null && value != null) {
				LyricLine(time = startMs.milliseconds, text = value)
			} else null
		}?.sortedBy { it.time }
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
		return input.lineSequence()
			.filter { it.isNotBlank() && it.startsWith("[") && it.contains("]") }
			.mapNotNull { line ->
				try {
					val close = line.indexOf(']')
					val timestamp = line.substring(1, close)
					val text = line.substring(close + 1).trim()

					if (!timestamp.contains(':') || timestamp.any { it.isLetter() }) return@mapNotNull null

					val parts = timestamp.split(':', '.')
					val minutes = parts[0].toLong()
					val seconds = parts[1].toLong()
					val hundredths = parts.getOrNull(2)?.toLong() ?: 0L

					val duration = minutes.minutes + seconds.seconds + (hundredths * 10).milliseconds

					LyricLine(time = duration, text = text)
				} catch (_: Exception) {
					null
				}
			}
			.toList()
			.sortedBy { it.time }
	}
}

class LyricsRepository(
	private val client: HttpClient = HttpClient(),
	private val settings: Settings = Settings()
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

	suspend fun fetchLyrics(track: Track): LyricsResult? {
		val currentConfig = getConfig()
		for (provider in currentConfig.priority) {
			try {
				val rawContent = when (provider) {
					LyricsProvider.LYRICS_PLUS -> fetchRawLyricsPlus(track, currentConfig)
					LyricsProvider.SUBSONIC -> fetchRawSubsonic(track)
					LyricsProvider.LRCLIB -> fetchRawLrcLib(track, currentConfig)
				}
				val parsedLyrics = rawContent?.let { LyricsContentParser.parse(it) }
				if (!parsedLyrics.isNullOrEmpty()) {
					return LyricsResult(
						lines = parsedLyrics,
						provider = provider
					)
				}
			} catch (e: Exception) {
				println("Provider ${provider.name} failed: ${e.message}")
				continue
			}
		}
		return null
	}

	private suspend fun fetchRawSubsonic(track: Track): String? {
		return runCatching {
			SessionManager.api.getLyricsBySongId(track.id)
		}.getOrNull()
	}

	private suspend fun fetchRawLrcLib(track: Track, config: LyricsConfig): String? {
		val artist = track.artist ?: return null
		val album = track.album ?: return null
		val duration = track.duration ?: return null

		return try {
			val response = client.get(config.lrcLibBaseUrl) {
				parameter("track_name", track.title)
				parameter("artist_name", artist)
				parameter("album_name", album)
				parameter("duration", duration)
				accept(ContentType.Application.Json)
			}
			if (response.status.isSuccess()) response.bodyAsText() else null
		} catch (_: Exception) {
			null
		}
	}

	private suspend fun fetchRawLyricsPlus(track: Track, config: LyricsConfig): String? {
		val artist = track.artist ?: return null

		for (baseUrl in config.lyricsPlusMirrors) {
			try {
				val response = client.get("$baseUrl/v2/lyrics/get") {
					parameter("title", track.title)
					parameter("artist", artist)
					parameter("album", track.album)
					parameter("duration", track.duration)
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
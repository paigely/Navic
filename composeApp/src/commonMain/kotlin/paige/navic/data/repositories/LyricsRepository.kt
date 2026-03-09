package paige.navic.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

enum class LyricsProvider { LYRICS_PLUS, SUBSONIC, LRCLIB }

@Serializable
private data class ApiError(
	val message: String? = null,
	val name: String? = null,
	val code: Int? = null
)

@Serializable
private data class LrcLibResponse(
	val syncedLyrics: String? = null,
	val plainLyrics: String? = null
)

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


class LyricsRepository(
	baseClient: HttpClient = HttpClient()
) {
	private val jsonConfig = Json {
		isLenient = true
		explicitNulls = false
		prettyPrint = true
		ignoreUnknownKeys = true
	}

	private val client = baseClient.config {
		install(ContentNegotiation) {
			json(json = jsonConfig)
		}
		install(DefaultRequest) {
			url("https://lrclib.net/")
		}
	}

	private suspend inline fun <reified T> HttpResponse.getOrThrow(): T {
		if (this.status.isSuccess()) {
			return this.body<T>()
		} else {
			val errorBody = runCatching { this.body<ApiError>() }.getOrNull()
			throw Exception(errorBody?.message ?: "Request failed with status: ${this.status}")
		}
	}

	private fun parseLrcLibLyrics(input: String): List<LyricLine> =
		input.lineSequence()
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

					val duration =
						minutes.minutes +
							seconds.seconds +
							(hundredths * 10).milliseconds

					LyricLine(time = duration, text = text)
				} catch (_: Exception) {
					null
				}
			}
			.toList()
			.sortedBy { it.time }

	suspend fun fetchLyrics(
		track: Track,
		priority: List<LyricsProvider> = listOf(
			LyricsProvider.LYRICS_PLUS,
			LyricsProvider.SUBSONIC,
			LyricsProvider.LRCLIB
		)
	): List<LyricLine>? {
		for (provider in priority) {
			try {
				val lyrics = when (provider) {
					LyricsProvider.LYRICS_PLUS -> fetchLyricsPlus(track)
					LyricsProvider.SUBSONIC -> fetchSubsonicLyrics(track)
					LyricsProvider.LRCLIB -> fetchLrcLibLyrics(track)
				}
				if (!lyrics.isNullOrEmpty()) return lyrics
			} catch (e: Exception) {
				println("Provider ${provider.name} failed: ${e.message}")
				continue
			}
		}
		return null
	}

	private suspend fun fetchSubsonicLyrics(track: Track): List<LyricLine>? {
		return runCatching {
			val jsonString = SessionManager.api.getLyricsBySongId(track.id)
			val jsonRoot = jsonConfig.parseToJsonElement(jsonString).jsonObject

			val subsonicResponse = jsonRoot["subsonic-response"]?.jsonObject
			if (subsonicResponse?.get("status")?.jsonPrimitive?.contentOrNull == "failed") {
				val errorJson = subsonicResponse["error"]?.jsonObject
				val apiError = ApiError(
					message = errorJson?.get("message")?.jsonPrimitive?.contentOrNull,
					code = errorJson?.get("code")?.jsonPrimitive?.contentOrNull?.toIntOrNull()
				)
				throw Exception(apiError.message ?: "Subsonic API Error")
			}

			val structuredLyrics = subsonicResponse?.get("lyricsList")
				?.jsonObject?.get("structuredLyrics")
				?.jsonArray

			val syncedLyrics = structuredLyrics
				?.firstOrNull { it.jsonObject["synced"]?.jsonPrimitive?.booleanOrNull == true }
				?: structuredLyrics?.firstOrNull()

			syncedLyrics?.jsonObject?.get("line")?.jsonArray?.mapNotNull { line ->
				val startMs = line.jsonObject["start"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
				val value = line.jsonObject["value"]?.jsonPrimitive?.contentOrNull
				if (startMs != null && value != null) {
					LyricLine(time = startMs.milliseconds, text = value)
				} else null
			}?.sortedBy { it.time }
		}.getOrElse { throw it }
	}

	private suspend fun fetchLrcLibLyrics(track: Track): List<LyricLine>? {
		val artist = track.artist ?: return null
		val album = track.album ?: return null
		val duration = track.duration ?: return null

		return try {
			val response = client.get("https://lrclib.net/api/get") {
				parameter("track_name", track.title)
				parameter("artist_name", artist)
				parameter("album_name", album)
				parameter("duration", duration)
				accept(ContentType.Application.Json)
			}

			val lrcData = response.body<LrcLibResponse>()
			val syncedStr = lrcData.syncedLyrics

			if (!syncedStr.isNullOrEmpty()) {
				parseLrcLibLyrics(syncedStr)
			} else {
				null
			}
		} catch (_: Exception) {
			null
		}
	}

	private suspend fun fetchLyricsPlus(track: Track): List<LyricLine>? {
		val artist = track.artist ?: return null
		val mirrors = listOf(
			"https://lyricsplus.atomix.one",
			"https://lyricsplus-seven.vercel.app",
			"https://lyricsplus.prjktla.workers.dev"
		)

		for (baseUrl in mirrors) {
			try {
				val response = client.get("$baseUrl/v2/lyrics/get") {
					parameter("title", track.title)
					parameter("artist", artist)
					parameter("album", track.album)
					parameter("duration", track.duration)
					accept(ContentType.Application.Json)
				}

				val apiResponse = response.getOrThrow<YoulyResponse>()
				if (apiResponse.lyrics.isNotEmpty()) {
					return apiResponse.lyrics.map { line ->
						LyricLine(
							time = line.time.milliseconds,
							text = line.text,
							words = line.syllabus?.map { syl ->
								LyricWord(syl.time.milliseconds, syl.duration.milliseconds, syl.text)
							}
						)
					}
				}
			} catch (_: Exception) {
			}
		}
		return null
	}
}
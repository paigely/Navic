package paige.navic.data.repositories

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
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

@Serializable
private data class Lyrics(
	val id: Int,
	val trackName: String,
	val artistName: String,
	val albumName: String,
	val duration: Float,
	val instrumental: Boolean,
	val plainLyrics: String,
	val syncedLyrics: String
)

@Serializable
private data class ApiError(
	val message: String?,
	val name: String?,
	val code: Int?
)

class LyricsRepository(
	baseClient: HttpClient = HttpClient()
) {
	private val client = baseClient.config {
		install(ContentNegotiation) {
			json(
				json = Json {
					isLenient = true
					explicitNulls = false
					prettyPrint = true
					ignoreUnknownKeys = true
				}
			)
		}
		install(DefaultRequest) {
			url("https://lrclib.net/")
		}
	}

	private fun parseLyrics(input: String): List<Pair<Duration, String>> =
		input.lineSequence()
			.filter { it.isNotBlank() }
			.map { line ->
				val close = line.indexOf(']')
				val timestamp = line.substring(1, close)
				val text = line.substring(close + 1).trim()

				val parts = timestamp.split(':', '.')
				val minutes = parts[0].toLong()
				val seconds = parts[1].toLong()
				val hundredths = parts[2].toLong()

				val duration =
					minutes.minutes +
						seconds.seconds +
						(hundredths * 10).milliseconds

				duration to text
			}
			.toList()
			.sortedBy { it.first }

	suspend fun fetchLyrics(track: Track): List<Pair<Duration, String>>? {
		val artist = track.artist ?: return null
		val album = track.album ?: return null
		val duration = track.duration ?: return null

		runCatching {
			val jsonString = SessionManager.api.getLyricsBySongId(track.id)
			val json = Json.parseToJsonElement(jsonString)

			val structuredLyrics = json.jsonObject["subsonic-response"]
				?.jsonObject?.get("lyricsList")
				?.jsonObject?.get("structuredLyrics")
				?.jsonArray

			val syncedLyrics = structuredLyrics
				?.firstOrNull { it.jsonObject["synced"]?.jsonPrimitive?.booleanOrNull == true }
				?: structuredLyrics?.firstOrNull()

			val lines = syncedLyrics?.jsonObject?.get("line")?.jsonArray
			if (!lines.isNullOrEmpty()) {
				return lines.mapNotNull { line ->
					val startMs = line.jsonObject["start"]?.jsonPrimitive?.contentOrNull?.toLongOrNull()
					val value = line.jsonObject["value"]?.jsonPrimitive?.contentOrNull
					if (startMs != null && value != null) startMs.milliseconds to value else null
				}.sortedBy { it.first }
			}
		}

		val response = client.get("api/get") {
			parameter("track_name", track.title)
			parameter("artist_name", artist)
			parameter("album_name", album)
			parameter("duration", duration)
			accept(ContentType.Application.Json)
		}

		return runCatching {
			parseLyrics(response.body<Lyrics>().syncedLyrics)
		}.getOrElse { exception ->
			val error = runCatching {
				response.body<ApiError>()
			}.getOrElse { throw exception }
			if (error.name == "TrackNotFound") {
				return null
			} else {
				throw Exception(error.message)
			}
		}
	}
}
package paige.navic.data.repository

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import paige.subsonic.api.model.Track
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
	val message: String,
	val name: String,
	val statusCode: Int
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

		return try {
			parseLyrics(
				client.get("api/get") {
					parameter("track_name", track.title)
					parameter("artist_name", artist)
					parameter("album_name", album)
					parameter("duration", duration)
					accept(ContentType.Application.Json)
				}.body<Lyrics>().syncedLyrics
			)
		} catch (e: ClientRequestException) {
			if (e.response.status == HttpStatusCode.NotFound) {
				val error = e.response.body<ApiError>()
				if (error.name == "TrackNotFound")
					null
				else throw e
			} else { throw e }
		}
	}
}
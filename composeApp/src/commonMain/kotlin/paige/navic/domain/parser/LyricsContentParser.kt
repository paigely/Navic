package paige.navic.domain.parser

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import paige.navic.domain.models.lyrics.LyricsLine
import paige.navic.domain.models.lyrics.LyricsWord
import paige.navic.util.core.Logger
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

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

	fun parse(content: String): List<LyricsLine>? {
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

	private fun parseJson(jsonString: String): List<LyricsLine>? {
		val jsonObject = jsonParser.parseToJsonElement(jsonString).jsonObject

		val syncedStr = jsonObject["syncedLyrics"]?.jsonPrimitive?.contentOrNull
		if (!syncedStr.isNullOrEmpty()) {
			return parseLrc(syncedStr)
		}

		val plainStr = jsonObject["plainLyrics"]?.jsonPrimitive?.contentOrNull
		if (!plainStr.isNullOrEmpty()) {
			return plainStr.lineSequence()
				.map { LyricsLine(text = it.trim()) }
				.toList()
		}

		if (jsonObject.containsKey("lyrics")) {
			val youlyResponse = jsonParser.decodeFromString<YoulyResponse>(jsonString)
			return parseYoulyResponse(youlyResponse)
		}

		return null
	}

	private fun parseYoulyResponse(response: YoulyResponse): List<LyricsLine>? {
		if (response.lyrics.isEmpty()) return null
		return response.lyrics.map { line ->
			LyricsLine(
				time = line.time.milliseconds,
				text = line.text,
				words = line.syllabus?.map { syl ->
					LyricsWord(syl.time.milliseconds, syl.duration.milliseconds, syl.text)
				}
			)
		}.sortedBy { it.time }
	}

	private fun parseLrc(input: String): List<LyricsLine> {
		val lines = input.lineSequence().toList()

		if (!input.contains("[")) {
			return lines.map { LyricsLine(text = it.trim()) }
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
							return@mapNotNull if (text.isNotEmpty()) LyricsLine(text = text) else null
						}

						val parts = timestamp.split(':', '.')
						val minutes = parts[0].toLong()
						val seconds = parts[1].toLong()
						val hundredths = parts.getOrNull(2)?.toLong() ?: 0L
						val duration =
							minutes.minutes + seconds.seconds + (hundredths * 10).milliseconds

						LyricsLine(time = duration, text = text)
					} else {
						LyricsLine(text = line.trim())
					}
				} catch (_: Exception) {
					null
				}
			}
			.toList()
			.sortedBy { it.time }
	}
}

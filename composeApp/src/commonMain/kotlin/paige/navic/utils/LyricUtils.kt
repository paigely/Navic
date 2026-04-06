package paige.navic.utils

import paige.navic.domain.repositories.LyricWord
import kotlin.time.Duration

fun List<LyricWord>.calculateWordProgress(
	fullText: String,
	currentDuration: Duration
): Float {
	if (isEmpty() || fullText.isEmpty()) return 0f

	val currentMs = currentDuration.inWholeMilliseconds
	val totalChars = fullText.length.toFloat()

	if (currentMs < first().time.inWholeMilliseconds) return 0f

	var currentCharacterIndex = 0

	for (i in indices) {
		val word = get(i)
		val wordStartMs = word.time.inWholeMilliseconds
		val wordEndMs = wordStartMs + word.duration.inWholeMilliseconds

		val wordIndexInString = fullText.indexOf(word.text, startIndex = currentCharacterIndex, ignoreCase = true)

		if (wordIndexInString == -1) {
			continue
		}

		if (currentMs in wordStartMs until wordEndMs) {
			val wordProgress = (currentMs - wordStartMs).toFloat() / word.duration.inWholeMilliseconds.coerceAtLeast(1)
			val charProgressWithinWord = word.text.length * wordProgress

			return (wordIndexInString + charProgressWithinWord) / totalChars
		}

		if (currentMs < wordStartMs) {
			return wordIndexInString / totalChars
		}

		currentCharacterIndex = wordIndexInString + word.text.length
	}

	return 1f
}

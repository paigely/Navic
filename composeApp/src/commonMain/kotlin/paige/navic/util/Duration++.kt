package paige.navic.util

import kotlin.time.Duration

fun Duration.toHumanReadable(): String =
	toComponents { days, hours, minutes, seconds, _ ->
		buildString {
			if (days > 0) append("${days}d ")
			if (hours > 0) append("${hours}h ")
			if (minutes > 0) append("${minutes}m ")
			if (seconds > 0 || isEmpty()) append("${seconds}s")
		}.trim()
	}

fun Duration.toHHMMSS(): String {
	val totalSeconds = inWholeSeconds

	val hours = totalSeconds / 3600
	val minutes = (totalSeconds % 3600) / 60
	val seconds = totalSeconds % 60

	fun Long.twoDigits() = toString().padStart(2, '0')

	return if (hours > 0) {
		"${hours.twoDigits()}:${minutes.twoDigits()}:${seconds.twoDigits()}"
	} else {
		"${minutes.twoDigits()}:${seconds.twoDigits()}"
	}
}

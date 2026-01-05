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
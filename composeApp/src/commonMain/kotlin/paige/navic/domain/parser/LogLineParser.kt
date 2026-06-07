package paige.navic.domain.parser

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import paige.navic.ui.theme.warning

data class LogLine(
	val type: LogLineType,
	val text: String,
	val rawText: String
)

enum class LogLineType {
	Info,
	Warning,
	Error,
	Debug,
	Verbose;

	@Composable
	fun backgroundColor() = when (this) {
		Info -> MaterialTheme.colorScheme.primary
		Warning -> MaterialTheme.colorScheme.warning
		Error -> MaterialTheme.colorScheme.error
		Debug -> MaterialTheme.colorScheme.secondary
		Verbose -> MaterialTheme.colorScheme.secondary
	}

	@Composable
	fun contentColor() = when (this) {
		Info -> MaterialTheme.colorScheme.onPrimary
		Warning -> MaterialTheme.colorScheme.surface
		Error -> MaterialTheme.colorScheme.onError
		Debug -> MaterialTheme.colorScheme.onSecondary
		Verbose -> MaterialTheme.colorScheme.onSecondary
	}

	companion object {
		fun fromChar(char: Char?) = when (char) {
			'I' -> Info
			'W' -> Warning
			'E' -> Error
			'D' -> Debug
			'V' -> Verbose
			else -> Info
		}
	}
}

object LogLineParser {
	fun parseString(rawText: String) = LogLine(
		text = rawText.drop(2),
		rawText = rawText,
		type = LogLineType.fromChar(rawText.firstOrNull())
	)
}

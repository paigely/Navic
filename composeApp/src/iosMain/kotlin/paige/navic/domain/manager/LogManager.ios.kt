package paige.navic.domain.manager

import paige.navic.domain.parser.LogLine

actual class LogManager {
	actual val logs: List<LogLine>
		get() = TODO()

	actual fun startStreaming() {}
	actual fun stopStreaming() {}
	actual fun clearLogs() {}
}

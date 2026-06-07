package paige.navic.domain.manager

import paige.navic.domain.parser.LogLine

expect class LogManager {
	val logs: List<LogLine>
	fun startStreaming()
	fun stopStreaming()
	fun clearLogs()
}

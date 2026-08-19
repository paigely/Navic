package paige.navic.domain.manager

import kotlinx.coroutines.flow.Flow
import paige.navic.domain.parser.LogLine

expect class LogManager {
	fun clearLogs()
	fun logFlow(): Flow<LogLine>
}

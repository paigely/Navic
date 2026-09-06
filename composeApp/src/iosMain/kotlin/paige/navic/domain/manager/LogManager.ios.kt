package paige.navic.domain.manager

import kotlinx.coroutines.flow.Flow
import paige.navic.domain.parser.LogLine

actual class LogManager {
	actual fun clearLogs() { TODO() }
	actual fun logFlow(): Flow<LogLine> = TODO()
}

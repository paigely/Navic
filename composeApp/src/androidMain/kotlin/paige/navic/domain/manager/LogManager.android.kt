package paige.navic.domain.manager

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import paige.navic.domain.parser.LogLine
import paige.navic.domain.parser.LogLineParser
import java.io.InputStreamReader

private const val LOG_SIZE = 500

actual class LogManager {
	private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
	private var readerJob: Job? = null

	private val _logs = mutableStateListOf<LogLine>()
	actual val logs: List<LogLine> = _logs

	actual fun startStreaming() {
		stopStreaming()
		_logs.clear()
		readerJob = scope.launch {
			val process = Runtime.getRuntime().exec(arrayOf("logcat", "--format=tag"))
			val reader = InputStreamReader(process.inputStream).buffered()

			while (isActive) {
				val nextLine = reader.readLine() ?: break
				val parsedLine = LogLineParser.parseString(nextLine)
				_logs.add(parsedLine)
				if (_logs.size > LOG_SIZE) {
					_logs.removeAt(0)
				}
			}

			reader.close()
			process.destroy()
		}
	}

	actual fun stopStreaming() {
		readerJob?.cancel()
		readerJob = null
	}

	actual fun clearLogs() {
		Runtime.getRuntime().exec(arrayOf("logcat", "-c"))
		_logs.clear()
	}
}

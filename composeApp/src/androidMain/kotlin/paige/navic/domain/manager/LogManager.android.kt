package paige.navic.domain.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import paige.navic.domain.parser.LogLineParser

actual class LogManager {
	actual fun clearLogs() {
		Runtime.getRuntime().exec(arrayOf("logcat", "-c"))
	}

	actual fun logFlow() = flow {
		val process = Runtime.getRuntime().exec(arrayOf("logcat", "--format=tag"))
		val reader = process.inputStream.bufferedReader()
		var id = 0

		while (currentCoroutineContext().isActive) {
			val nextLine = reader.readLine() ?: break
			val parsedLine = LogLineParser.parseString(nextLine, id++)
			emit(parsedLine)
		}

		reader.close()
		process.destroy()
	}.flowOn(Dispatchers.IO)
}

package paige.navic.managers

import android.content.Context
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class StorageManager(
	private val context: Context,
	private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	actual fun getDownloadPath(songId: String, extension: String): String {
		val dir = File(context.filesDir, "downloads")
		if (!dir.exists()) dir.mkdirs()
		return File(dir, "$songId.$extension").absolutePath
	}

	actual fun deleteFile(path: String): Boolean {
		return File(path).delete()
	}

	actual fun getFileSize(path: String): Long {
		return try {
			File(path).length()
		} catch (_: Exception) {
			0L
		}
	}

	actual suspend fun saveFile(path: String, channel: ByteReadChannel) {
		withContext(dispatcher) {
			FileOutputStream(path).use { outputStream ->
				channel.copyTo(outputStream)
			}
		}
	}
}

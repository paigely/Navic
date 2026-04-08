package paige.navic.managers

import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

actual class StorageManager(
	private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	actual fun getDownloadPath(songId: String, extension: String): String {
		val home = System.getProperty("user.home")
		val os = System.getProperty("os.name").lowercase()
		val baseDir = when {
			os.contains("linux") -> {
				val xdgConfig = System.getenv("XDG_CONFIG_HOME")
				if (!xdgConfig.isNullOrBlank()) {
					File(xdgConfig, "navic")
				} else {
					File(home, ".config/navic")
				}
			}

			os.contains("mac") -> File(home, "Library/Application Support/Navic")
			os.contains("win") -> File(System.getenv("AppData"), "Navic")
			else -> File(home, ".navic")
		}
		val dir = File(baseDir, "downloads")
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

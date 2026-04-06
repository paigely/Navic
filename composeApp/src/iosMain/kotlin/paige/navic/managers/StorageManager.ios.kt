@file:OptIn(ExperimentalForeignApi::class)

package paige.navic.managers

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.Pinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.pin
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSFileSize
import platform.Foundation.NSNumber
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.cinterop.reinterpret
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSOutputStream
import platform.Foundation.outputStreamToFileAtPath

actual class StorageManager(
	private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
	actual fun getDownloadPath(songId: String, extension: String): String {
		val manager = NSFileManager.defaultManager
		val url = manager.URLsForDirectory(NSDocumentDirectory, NSUserDomainMask).first() as NSURL
		val dir = url.URLByAppendingPathComponent("downloads")!!
		if (!manager.fileExistsAtPath(dir.path!!)) {
			manager.createDirectoryAtURL(dir, true, null, null)
		}
		return dir.URLByAppendingPathComponent("$songId.$extension")!!.path!!
	}

	actual fun deleteFile(path: String): Boolean {
		return NSFileManager.defaultManager.removeItemAtPath(path, null)
	}

	actual fun getFileSize(path: String): Long {
		val manager = NSFileManager.defaultManager
		val attributes = manager.attributesOfItemAtPath(path, null)
		return (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
	}

	actual suspend fun saveFile(path: String, channel: ByteReadChannel) {
		withContext(dispatcher) {
			val outputStream = NSOutputStream.outputStreamToFileAtPath(path, false)
			outputStream.open()
			try {
				val buffer = ByteArray(64 * 1024)
				while (true) {
					val read = channel.readAvailable(buffer)
					if (read == -1) break
					if (read > 0) {
						buffer.usePinned { pinned ->
							outputStream.write(pinned.addressOf(0).reinterpret(), read.toULong())
						}
					}
				}
			} finally {
				outputStream.close()
			}
		}
	}

	private fun <T> ByteArray.usePinned(block: (Pinned<ByteArray>) -> T): T {
		val pinned = this.pin()
		try {
			return block(pinned)
		} finally {
			pinned.unpin()
		}
	}
}

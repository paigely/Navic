package paige.navic.managers


import io.ktor.utils.io.ByteReadChannel

expect class StorageManager {
	fun getDownloadPath(songId: String, extension: String): String
	fun deleteFile(path: String): Boolean
	fun getFileSize(path: String): Long
	suspend fun saveFile(path: String, channel: ByteReadChannel)
}

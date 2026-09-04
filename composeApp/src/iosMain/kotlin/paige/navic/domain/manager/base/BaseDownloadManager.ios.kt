package paige.navic.domain.manager.base

import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import paige.navic.domain.manager.StorageManager
import paige.navic.domain.models.DomainSong

actual class BaseDownloadManager(
	private val storageManager: StorageManager
) {
	private val client = HttpClient()

	actual suspend fun downloadAudio(
        song: DomainSong,
        url: String,
        extension: String,
        headers: Map<String, String>,
        onProgress: suspend (Float) -> Unit
	): String {
		val request = client.prepareRequest(url) {
			method = HttpMethod.Get
			headers.forEach { (key, value) ->
				header(key, value)
			}
			onDownload { bytesSentTotal, contentLength ->
				if (contentLength != null && contentLength > 0L) {
					onProgress((bytesSentTotal.toDouble() / contentLength).toFloat())
				}
			}
		}

		var finalPath = ""
		request.execute { response ->
			finalPath = storageManager.getDownloadPath(song.id, extension)
			storageManager.saveFile(finalPath, response.bodyAsChannel())
		}

		return finalPath
	}
}

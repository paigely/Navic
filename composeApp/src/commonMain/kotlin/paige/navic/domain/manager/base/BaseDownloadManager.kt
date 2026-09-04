package paige.navic.domain.manager.base

import paige.navic.domain.models.DomainSong

expect class BaseDownloadManager {
	suspend fun downloadAudio(
        song: DomainSong,
        url: String,
        extension: String,
        headers: Map<String, String>,
        onProgress: suspend (Float) -> Unit
	): String
}

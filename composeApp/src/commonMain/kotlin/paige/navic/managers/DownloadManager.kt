package paige.navic.managers

import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.entities.LyricEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.LyricRepository
import paige.navic.shared.Logger

class DownloadManager(
	private val platformContext: coil3.PlatformContext,
	private val downloadDao: DownloadDao,
	private val albumDao: AlbumDao,
	private val storageManager: StorageManager,
	private val lyricRepository: LyricRepository,
	private val lyricDao: LyricDao,
	private val scope: CoroutineScope,
	private val client: HttpClient = HttpClient()
) {
	private val activeDownloads = mutableMapOf<String, Job>()
	private val downloadSemaphore =
		Semaphore(10)// idk a good number, maybe u should be able to choose

	val allDownloads = downloadDao.getAllDownloads()
	val downloadCount = downloadDao.getDownloadsCount()
	val downloadSize = allDownloads.map { downloads ->
		downloads
			.filter { it.status == DownloadStatus.DOWNLOADED && it.filePath != null }
			.sumOf { storageManager.getFileSize(it.filePath!!) }
	}

	private val _downloadedSongs = MutableStateFlow<Map<String, String>>(emptyMap())
	val downloadedSongs: StateFlow<Map<String, String>> = _downloadedSongs.asStateFlow()

	init {
		scope.launch {
			allDownloads.collectLatest { downloads ->
				_downloadedSongs.value = downloads
					.filter { it.status == DownloadStatus.DOWNLOADED && it.filePath != null }
					.associate { it.songId to it.filePath!! }
			}
		}
	}

	fun getDownloadedFilePath(songId: String): String? {
		return _downloadedSongs.value[songId]
	}

	fun downloadSong(song: DomainSong) {
		if (activeDownloads.containsKey(song.id)) return

		val job = scope.launch(Dispatchers.IO) {
			downloadSemaphore.withPermit {
				executeDownloadProcess(song)
			}
		}
		activeDownloads[song.id] = job
	}

	suspend fun downloadCollection(collection: DomainSongCollection) {
		collection.songs
			.filter { !isDownloaded(it.id) }
			.forEach { downloadSong(it) }
	}

	fun cancelDownload(songId: String) {
		activeDownloads[songId]?.cancel()
		activeDownloads.remove(songId)
		scope.launch(Dispatchers.IO) {
			val existing = downloadDao.getDownloadById(songId)
			if (existing?.status == DownloadStatus.DOWNLOADING
				|| existing?.status == DownloadStatus.FAILED
			) {
				downloadDao.deleteDownload(songId)
			}
		}
	}

	fun cancelCollectionDownload(collection: DomainSongCollection) {
		collection.songs.forEach { song ->
			cancelDownload(song.id)
		}
	}

	fun deleteDownload(songId: String) {
		cancelDownload(songId)
		scope.launch {
			val download = downloadDao.getDownloadById(songId)
			download?.filePath?.let { storageManager.deleteFile(it) }
			downloadDao.deleteDownload(songId)
		}
	}

	fun deleteDownloadedCollection(collection: DomainSongCollection) {
		collection.songs.forEach { song ->
			deleteDownload(song.id)
		}
	}

	suspend fun isDownloaded(songId: String): Boolean {
		return downloadDao.getDownloadById(songId)?.status == DownloadStatus.DOWNLOADED
	}

	fun getCollectionDownloadStatus(songIds: List<String>): Flow<DownloadStatus> {
		return allDownloads.map { downloads ->
			val collectionDownloads = downloads.filter { it.songId in songIds }
			when {
				collectionDownloads.isEmpty() -> DownloadStatus.NOT_DOWNLOADED
				collectionDownloads.any { it.status == DownloadStatus.DOWNLOADING } -> DownloadStatus.DOWNLOADING
				collectionDownloads.any { it.status == DownloadStatus.FAILED } -> DownloadStatus.FAILED
				(collectionDownloads.size == songIds.size &&
					collectionDownloads.all { it.status == DownloadStatus.DOWNLOADED })
					-> DownloadStatus.DOWNLOADED

				else -> DownloadStatus.NOT_DOWNLOADED
			}
		}
	}

	fun clearAllDownloads() {
		scope.launch(Dispatchers.IO) {
			activeDownloads.values.forEach { it.cancel() }
			activeDownloads.clear()
			storageManager.clearDownloads()
			downloadDao.clearAllDownloads()
			Logger.i("DownloadManager", "cleared all downloads")
		}
	}

	private suspend fun executeDownloadProcess(song: DomainSong) {
		try {
			Logger.i("DownloadManager", "beginning download for ${song.id}")
			downloadDao.insertDownload(DownloadEntity(song.id, DownloadStatus.DOWNLOADING, 0f))

			cacheCoverArt(song.coverArtId)
			cacheAlbumCoverArt(song.albumId)
			cacheLyrics(song)
			downloadAudioFile(song)

		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to download song ${song.id}", e)
			downloadDao.insertDownload(DownloadEntity(song.id, DownloadStatus.FAILED, 0f))
		} finally {
			activeDownloads.remove(song.id)
		}
	}

	private suspend fun cacheCoverArt(coverId: String?) {
		if (coverId == null) return

		Logger.i("DownloadManager", "caching cover art for $coverId")
		val coverArtUrl = SessionManager.api.getCoverArtUrl(coverId, auth = true)

		val imageRequest = ImageRequest.Builder(platformContext)
			.data(coverArtUrl)
			.memoryCacheKey(coverId)
			.diskCacheKey(coverId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.build()

		SingletonImageLoader.get(platformContext).execute(imageRequest)
		Logger.i("DownloadManager", "cached cover art for $coverId")
	}

	private suspend fun cacheAlbumCoverArt(albumId: String?) {
		if (albumId == null) return

		try {
			val albumWithSongs = albumDao.getAlbumById(albumId)
			val albumCoverId = albumWithSongs?.album?.coverArtId

			if (albumCoverId != null) {
				Logger.i("DownloadManager", "Found album cover $albumCoverId for album $albumId")
				cacheCoverArt(albumCoverId)
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to cache album cover art for album $albumId", e)
		}
	}

	private suspend fun cacheLyrics(song: DomainSong) {
		Logger.i("DownloadManager", "caching lyrics for ${song.id}")
		try {
			val lyricsResult = lyricRepository.fetchLyrics(song)
			if (lyricsResult != null && lyricsResult.rawContent != null) {
				lyricDao.insertLyrics(
					LyricEntity(
						song.id,
						lyricsResult.rawContent,
						lyricsResult.provider
					)
				)
				Logger.i("DownloadManager", "cached lyrics for ${song.id}")
			}
		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to cache lyrics for ${song.id}", e)
		}
	}

	private suspend fun downloadAudioFile(song: DomainSong) {
		var lastProgress = 0f
		val request = client.prepareRequest(SessionManager.api.getStreamUrl(song.id)) {
			method = HttpMethod.Get
			onDownload { bytesSentTotal, contentLength ->
				if (contentLength != null && contentLength > 0f) {
					val progress = (bytesSentTotal.toDouble() / contentLength).toFloat()
					if (progress - lastProgress >= 0.01f || progress == 1f) {
						lastProgress = progress
						Logger.i("DownloadManager", "downloading ${song.id} $progress")
						scope.launch {
							downloadDao.updateProgress(
								song.id,
								DownloadStatus.DOWNLOADING,
								progress
							)
						}
					}
				} else {
					Logger.i("DownloadManager", "downloaded ${song.id}")
				}
			}
		}

		request.execute { response ->
			Logger.i("DownloadManager", "writing download for ${song.id}")
			val path = storageManager.getDownloadPath(song.id, song.fileExtension)
			storageManager.saveFile(path, response.bodyAsChannel())
			Logger.i("DownloadManager", "wrote download for ${song.id}")

			downloadDao.insertDownload(
				DownloadEntity(
					song.id,
					DownloadStatus.DOWNLOADED,
					1f,
					path
				)
			)
		}
	}
}

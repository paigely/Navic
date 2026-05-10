package paige.navic.managers

import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.size.Size
import io.ktor.client.HttpClient
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.header
import io.ktor.client.request.prepareRequest
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpMethod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.database.entities.LyricEntity
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.LyricRepository
import paige.navic.shared.Logger

@OptIn(ExperimentalCoroutinesApi::class)
class DownloadManager(
	private val platformContext: coil3.PlatformContext,
	private val downloadDao: DownloadDao,
	private val albumDao: AlbumDao,
	private val storageManager: StorageManager,
	private val lyricRepository: LyricRepository,
	private val lyricDao: LyricDao,
	private val scope: CoroutineScope
) {
	private val client = HttpClient {
		val customHeaders = Settings.shared.customHeadersMap()
		if (customHeaders.isNotEmpty()) {
			defaultRequest {
				customHeaders.forEach { (key, value) -> header(key, value) }
			}
		}
	}
	private val activeDownloadsMutex = Mutex()
	private val activeDownloads = mutableMapOf<String, Job>()
	private val downloadSemaphore = Semaphore(10)

	val allDownloads: Flow<List<DownloadEntity>> = SessionManager.activeServerId
		.filterNotNull()
		.flatMapLatest { serverId -> downloadDao.getAllDownloads(serverId) }

	val downloadCount: Flow<Int> = SessionManager.activeServerId
		.filterNotNull()
		.flatMapLatest { serverId -> downloadDao.getDownloadsCount(serverId) }

	val downloadSize: Flow<Long> = allDownloads.map { downloads ->
		downloads
			.filter { it.status == DownloadStatus.DOWNLOADED && it.filePath != null }
			.sumOf { storageManager.getFileSize(it.filePath!!) }
	}

	private val _downloadedSongs = MutableStateFlow<Map<String, String>>(emptyMap())
	val downloadedSongs: StateFlow<Map<String, String>> = _downloadedSongs.asStateFlow()

	private var libraryDownloadJob: Job? = null
	private val _isDownloadingLibrary = MutableStateFlow(false)
	val isDownloadingLibrary: StateFlow<Boolean> = _isDownloadingLibrary.asStateFlow()
	private val _libraryDownloadProgress = MutableStateFlow(0f)
	val libraryDownloadProgress: StateFlow<Float> = _libraryDownloadProgress.asStateFlow()

	init {
		scope.launch {
			allDownloads.collectLatest { downloads ->
				_downloadedSongs.value = downloads
					.filter { it.status == DownloadStatus.DOWNLOADED && it.filePath != null }
					.associate { it.songId to it.filePath!! }
			}
		}
	}

	private fun getJobKey(serverId: String, songId: String): String = "${serverId}_$songId"

	fun getDownloadedFilePath(songId: String): String? {
		return _downloadedSongs.value[songId]
	}

	fun downloadSong(song: DomainSong): Job {
		return scope.launch(Dispatchers.IO) {
			val serverId = SessionManager.activeServerId.value ?: return@launch
			val key = getJobKey(serverId, song.id)

			val alreadyActive = activeDownloadsMutex.withLock { activeDownloads.containsKey(key) }
			if (alreadyActive) return@launch

			try {
				activeDownloadsMutex.withLock { activeDownloads[key] = coroutineContext[Job]!! }

				downloadSemaphore.withPermit {
					executeDownloadProcess(song, serverId)
				}
			} finally {
				activeDownloadsMutex.withLock { activeDownloads.remove(key) }
			}
		}
	}

	suspend fun downloadCollection(collection: DomainSongCollection) {
		collection.songs
			.filter { !isDownloaded(it.id) }
			.forEach { downloadSong(it) }
	}

	fun downloadEntireLibrary(songs: List<DomainSong>) {
		if (_isDownloadingLibrary.value) return

		libraryDownloadJob = scope.launch(Dispatchers.IO) {
			try {
				_isDownloadingLibrary.value = true
				_libraryDownloadProgress.value = 0f

				val songsToDownload = songs.filter { !isDownloaded(it.id) }
				val totalToDownload = songsToDownload.size

				if (totalToDownload == 0) {
					_isDownloadingLibrary.value = false
					_libraryDownloadProgress.value = 1f
					return@launch
				}

				val downloadQueue = Channel<DomainSong>(Channel.UNLIMITED)
				songsToDownload.forEach { downloadQueue.trySend(it) }
				downloadQueue.close()

				var processedCount = 0
				val progressMutex = Mutex()

				val workers = List(10) {
					launch {
						for (song in downloadQueue) {
							downloadSong(song).join()

							progressMutex.withLock {
								processedCount++
								_libraryDownloadProgress.value = processedCount.toFloat() / totalToDownload.toFloat()
							}
						}
					}
				}

				workers.joinAll()
				_isDownloadingLibrary.value = false

			} catch (_: CancellationException) {
				_isDownloadingLibrary.value = false
				_libraryDownloadProgress.value = 0f
			}
		}
	}

	fun cancelAllActiveDownloads() {
		libraryDownloadJob?.cancel()
		libraryDownloadJob = null
		_isDownloadingLibrary.value = false
		_libraryDownloadProgress.value = 0f

		scope.launch(Dispatchers.IO) {
			val jobsToCancel = activeDownloadsMutex.withLock {
				val copy = activeDownloads.toMap()
				activeDownloads.clear()
				copy
			}

			jobsToCancel.forEach { (key, job) ->
				job.cancel()

				val parts = key.split("_", limit = 2)
				if (parts.size == 2) {
					val serverId = parts[0]
					val songId = parts[1]

					val existing = downloadDao.getDownloadById(songId, serverId)
					if (existing?.status == DownloadStatus.DOWNLOADING) {
						downloadDao.deleteDownload(songId, serverId)
					}
				}
			}
		}
	}

	fun cancelDownload(songId: String) {
		scope.launch(Dispatchers.IO) {
			val serverId = SessionManager.activeServerId.value ?: return@launch
			val key = getJobKey(serverId, songId)

			activeDownloadsMutex.withLock {
				activeDownloads[key]?.cancel()
				activeDownloads.remove(key)
			}

			val existing = downloadDao.getDownloadById(songId, serverId)
			if (existing?.status == DownloadStatus.DOWNLOADING
				|| existing?.status == DownloadStatus.FAILED
			) {
				downloadDao.deleteDownload(songId, serverId)
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
			val serverId = SessionManager.activeServerId.value ?: return@launch
			val download = downloadDao.getDownloadById(songId, serverId)
			download?.filePath?.let { storageManager.deleteFile(it) }
			downloadDao.deleteDownload(songId, serverId)
		}
	}

	fun deleteDownloadedCollection(collection: DomainSongCollection) {
		collection.songs.forEach { song ->
			deleteDownload(song.id)
		}
	}

	suspend fun isDownloaded(songId: String): Boolean {
		val serverId = SessionManager.activeServerId.value ?: return false
		return downloadDao.getDownloadById(songId, serverId)?.status == DownloadStatus.DOWNLOADED
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
			val serverId = SessionManager.activeServerId.value ?: return@launch
			cancelAllActiveDownloads()
			storageManager.clearDownloads()
			downloadDao.clearDownloadsForServer(serverId)
			Logger.i("DownloadManager", "cleared all downloads for server $serverId")
		}
	}

	private suspend fun executeDownloadProcess(song: DomainSong, serverId: String) {
		val key = getJobKey(serverId, song.id)
		try {
			Logger.i("DownloadManager", "beginning download for ${song.id} on server $serverId")
			downloadDao.insertDownload(
				DownloadEntity(
					songId = song.id,
					serverId = serverId,
					status = DownloadStatus.DOWNLOADING,
					progress = 0f
				)
			)

			cacheCoverArt(song.coverArtId)
			cacheAlbumCoverArt(song.albumId, serverId)
			cacheLyrics(song, serverId)
			downloadAudioFile(song, serverId)

		} catch (e: Exception) {
			if (e is CancellationException) throw e
			Logger.e("DownloadManager", "Failed to download song ${song.id}", e)
			downloadDao.insertDownload(
				DownloadEntity(
					songId = song.id,
					serverId = serverId,
					status = DownloadStatus.FAILED,
					progress = 0f
				)
			)
		} finally {
			activeDownloadsMutex.withLock {
				activeDownloads.remove(key)
			}
		}
	}

	private suspend fun cacheCoverArt(coverId: String?) {
		if (coverId == null) return

		Logger.i("DownloadManager", "caching cover art for $coverId")
		val coverArtUrl = SessionManager.api.getCoverArtUrl(coverId, auth = true)

		val imageRequest = ImageRequest.Builder(platformContext)
			.data(coverArtUrl)
			.size(Size.ORIGINAL)
			.memoryCacheKey(coverId)
			.diskCacheKey(coverId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.DISABLED)
			.build()

		SingletonImageLoader.get(platformContext).execute(imageRequest)
		Logger.i("DownloadManager", "cached cover art for $coverId")
	}

	private suspend fun cacheAlbumCoverArt(albumId: String?, serverId: String) {
		if (albumId == null) return

		try {
			val albumWithSongs = albumDao.getAlbumById(albumId, serverId)
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

	private suspend fun cacheLyrics(song: DomainSong, serverId: String) {
		Logger.i("DownloadManager", "caching lyrics for ${song.id}")
		try {
			val lyricsResult = lyricRepository.fetchLyrics(song)
			if (lyricsResult != null && lyricsResult.rawContent != null) {
				lyricDao.insertLyrics(
					LyricEntity(
						serverId,
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

	private suspend fun downloadAudioFile(song: DomainSong, serverId: String) {
		var lastProgress = 0f
		var progressJob: Job? = null

		val request = client.prepareRequest(SessionManager.api.getStreamUrl(song.id)) {
			method = HttpMethod.Get
			onDownload { bytesSentTotal, contentLength ->
				if (contentLength != null && contentLength > 0L) {
					val progress = (bytesSentTotal.toDouble() / contentLength).toFloat()
					if (progress - lastProgress >= 0.01f || progress == 1f) {
						lastProgress = progress
						Logger.i("DownloadManager", "downloading ${song.id} $progress")

						progressJob?.cancel()

						progressJob = scope.launch {
							downloadDao.updateProgress(
								songId = song.id,
								serverId = serverId,
								status = DownloadStatus.DOWNLOADING,
								progress = progress
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

			progressJob?.cancel()

			downloadDao.insertDownload(
				DownloadEntity(
					songId = song.id,
					serverId = serverId,
					status = DownloadStatus.DOWNLOADED,
					progress = 1f,
					filePath = path
				)
			)
		}
	}
}

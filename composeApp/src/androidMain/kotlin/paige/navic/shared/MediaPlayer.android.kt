package paige.navic.shared

import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainRadio
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.domain.repositories.SongRepository
import paige.navic.managers.AndroidScrobbleManager
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager
import paige.navic.ui.components.common.CoilBitmapLoader
import paige.navic.utils.effectiveGain
import java.io.File
import kotlin.time.Duration

class PlaybackService : MediaSessionService(), KoinComponent {
	private var mediaSession: MediaSession? = null
	private val serviceScope = MainScope()
	private var scrobbleManager: AndroidScrobbleManager? = null
	private val resourceProvider: ResourceProvider by inject()

	private val connectivityManager: ConnectivityManager by inject()

	private val syncManager: SyncManager by inject()

	@OptIn(UnstableApi::class)
	override fun onCreate() {
		super.onCreate()
		val loadControl = DefaultLoadControl.Builder()
			.setBufferDurationsMs(
				/* minBufferMs = */ 32_000,
				/* maxBufferMs = */ 64_000,
				/* bufferForPlaybackMs = */ 2_500,
				/* bufferForPlaybackAfterRebufferMs = */ 5_000
			)
			.setBackBuffer(10_000, true)
			.build()

		val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
			.build().apply {
				setSmallIcon(resourceProvider.icNavic)
			}

		val player = ExoPlayer.Builder(this)
			.setLoadControl(loadControl)
			.setHandleAudioBecomingNoisy(true)
			.setWakeMode(C.WAKE_MODE_NETWORK)
			.build()
			.apply {
				setAudioAttributes(
					AudioAttributes.Builder()
						.setUsage(C.USAGE_MEDIA)
						.setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
						.build(),
					true
				)
				setMediaNotificationProvider(notificationProvider)
				trackSelectionParameters =
					trackSelectionParameters.buildUpon().setAudioOffloadPreferences(
						TrackSelectionParameters.AudioOffloadPreferences
							.Builder()
							.setIsGaplessSupportRequired(Settings.shared.gaplessPlayback)
							.setAudioOffloadMode(
								if (Settings.shared.audioOffload) {
									TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
								} else {
									TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
								}
							)
							.build()
					).build()
			}

		scrobbleManager =
			AndroidScrobbleManager(player, serviceScope, connectivityManager, syncManager)

		val sessionIntent = applicationContext.packageManager
			.getLaunchIntentForPackage(applicationContext.packageName)
			?.apply {
				flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or
					Intent.FLAG_ACTIVITY_CLEAR_TOP
			}

		val sessionPendingIntent = PendingIntent.getActivity(
			this,
			0,
			sessionIntent,
			PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
		)

		mediaSession = MediaSession.Builder(this, player)
			.setSessionActivity(sessionPendingIntent)
			.setBitmapLoader(CoilBitmapLoader(this))
			.build()
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return mediaSession
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		onDestroy()
	}

	override fun onDestroy() {
		scrobbleManager?.release()
		serviceScope.cancel()
		stopForeground(STOP_FOREGROUND_REMOVE)
		mediaSession?.run {
			player.stop()
			player.release()
			release()
		}
		super.onDestroy()
		mediaSession = null
		stopSelf()
	}

	companion object {
		fun newSessionToken(context: Context): SessionToken {
			return SessionToken(context, ComponentName(context, PlaybackService::class.java))
		}
	}
}

class AndroidMediaPlayerViewModel(
	private val application: Application,
	stateRepository: PlayerStateRepository,
	private val albumDao: AlbumDao,
	downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : MediaPlayerViewModel(
	stateRepository = stateRepository,
	downloadManager = downloadManager,
	connectivityManager = connectivityManager
) {
	private var controller: MediaController? = null
	private var controllerFuture: ListenableFuture<MediaController>? = null

	private var loadingCollectionId: String? = null

	private var pendingSyncState: PlayerUiState? = null

	init {
		connectToService()
	}

	private fun connectToService() {
		viewModelScope.launch {
			val sessionToken = PlaybackService.newSessionToken(application)
			controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
			controllerFuture?.addListener({
				controller = controllerFuture?.get()
				setupController()
			}, MoreExecutors.directExecutor())
		}
	}

	private fun getStreamUrl(id: String) =
		when (connectivityManager.isOnCellular) {
			true -> SessionManager.api.getStreamUrl(
				id,
				Settings.shared.streamingQualityCellular.bitrateAndroid,
				Settings.shared.streamingQualityCellular.containerAndroid
			).toUri()

			false -> SessionManager.api.getStreamUrl(
				id,
				Settings.shared.streamingQualityWifi.bitrateAndroid,
				Settings.shared.streamingQualityWifi.containerAndroid
			).toUri()
		}

	private fun setupController() {
		viewModelScope.launch {
			controller?.apply {
				addListener(object : Player.Listener {
					override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
						updatePlaybackState()

						mediaItem?.mediaId?.let { id ->
							if (!isAvailable(id)) {
								controller?.seekToNextMediaItem()
							}
						}
					}

					override fun onIsPlayingChanged(isPlaying: Boolean) {
						_uiState.update { it.copy(isPaused = !isPlaying) }
						if (isPlaying) startProgressLoop()
						val intent =
							Intent("${application.packageName}.NOW_PLAYING_UPDATED").apply {
								setPackage(application.packageName)
								putExtra("isPlaying", isPlaying)
								putExtra(
									"title",
									_uiState.value.currentSong?.title ?: "Unknown song"
								)
								putExtra(
									"artist",
									_uiState.value.currentSong?.artistName ?: "Unknown artist"
								)
								putExtra(
									"artUrl",
									_uiState.value.currentSong?.coverArtId?.let { id ->
										SessionManager.api.getCoverArtUrl(id, auth = true)
									})
							}

						application.sendBroadcast(intent)
					}

					override fun onPlaybackStateChanged(playbackState: Int) {
						_uiState.update { it.copy(isLoading = playbackState == Player.STATE_BUFFERING) }
						updatePlaybackState()
					}

					override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
						_uiState.update { it.copy(isShuffleEnabled = shuffleModeEnabled) }
					}

					override fun onRepeatModeChanged(repeatMode: Int) {
						_uiState.update { it.copy(repeatMode = repeatMode) }
					}
				})
				updatePlaybackState()

				downloadManager.allDownloads.first()
				pendingSyncState?.let { state ->
					syncPlayerWithState(state)
					pendingSyncState = null
				}

				downloadManager.downloadedSongs.collectLatest { downloadedMap ->
					val player = controller ?: return@collectLatest

					for (i in 0 until player.mediaItemCount) {
						val item = player.getMediaItemAt(i)
						val id = item.mediaId
						val localPath = downloadedMap[id]

						val isCurrentlyLocal = item.localConfiguration?.uri?.scheme == "file"

						if (localPath != null && !isCurrentlyLocal) {
							val newItem = item.buildUpon()
								.setUri(File(localPath).toUri())
								.build()
							player.replaceMediaItem(i, newItem)
						} else if (localPath == null && isCurrentlyLocal) {
							val newItem = item.buildUpon()
								.setUri(getStreamUrl(id))
								.build()
							player.replaceMediaItem(i, newItem)
						}
					}
				}
			}
		}
	}

	private fun refreshCurrentCollection(albumId: String) {
		if (loadingCollectionId == albumId) return
		loadingCollectionId = albumId

		viewModelScope.launch {
			runCatching {
				val album = albumDao.getAlbumById(albumId)

				_uiState.update { it.copy(currentCollection = album?.toDomainModel()) }
			}.onFailure {
				loadingCollectionId = null
			}
		}
	}

	private fun updatePlaybackState() {
		viewModelScope.launch {
			controller?.let { player ->
				val index = player.currentMediaItemIndex
				val currentSong = _uiState.value.queue.getOrNull(index)

				val derivedCollection = currentSong?.let { song ->
					val stateCollection = _uiState.value.currentCollection

					if (stateCollection?.id == song.albumId.toString()) {
						stateCollection
					} else {
						refreshCurrentCollection(song.albumId.toString())
						null
					}
				}

				_uiState.update { state ->
					state.copy(
						currentIndex = index,
						currentSong = currentSong,
						currentCollection = derivedCollection ?: state.currentCollection,
						isPaused = !player.isPlaying,
						isShuffleEnabled = player.shuffleModeEnabled,
						repeatMode = player.repeatMode
					)
				}
				applyReplayGain()
				updateProgress()
			}
		}
	}

	private fun applyReplayGain() {
		viewModelScope.launch {
			if (Settings.shared.replayGain) {
				(_uiState.value.currentSong)?.replayGain?.let { replayGain ->
					controller?.volume = replayGain.effectiveGain()
				}
			} else {
				controller?.volume = 1f
			}
		}
	}

	override fun syncPlayerWithState(state: PlayerUiState) {
		viewModelScope.launch {
			val player = controller

			if (player == null) {
				pendingSyncState = state
				return@launch
			}

			if (state.queue.isEmpty() || player.mediaItemCount > 0) return@launch

			val mediaItems = state.queue.map { it.toMediaItem() }

			player.setMediaItems(mediaItems)

			player.shuffleModeEnabled = state.isShuffleEnabled
			player.repeatMode = state.repeatMode

			val index = if (state.currentIndex in 0 until mediaItems.size) state.currentIndex else 0

			val songDurationMs = state.queue.getOrNull(index)?.duration?.inWholeMilliseconds ?: 0L

			val position = if (songDurationMs > 0) {
				(state.progress * songDurationMs).toLong()
			} else {
				0L
			}

			player.seekTo(index, position)
			player.prepare()
		}
	}

	private fun startProgressLoop() {
		viewModelScope.launch {
			while (controller?.isPlaying == true) {
				val player = controller ?: break
				val duration = player.duration.coerceAtLeast(1)
				val progress =
					(player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
				_uiState.update { it.copy(progress = progress) }
				delay(200)
			}
		}
	}

	private fun updateProgress() {
		viewModelScope.launch {
			controller?.let { player ->
				val duration = player.duration.coerceAtLeast(1)
				val pos = player.currentPosition
				val progress = (pos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
				_uiState.update { it.copy(progress = progress) }
			}
		}
	}

	override fun addToQueueSingle(song: DomainSong) {
		viewModelScope.launch {
			controller?.addMediaItem(song.toMediaItem())
			_uiState.update { state ->
				val newQueue = state.queue + song
				state.copy(
					queue = newQueue,
					currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
					currentSong = if (state.currentIndex == -1) song else state.currentSong
				)
			}
		}
	}

	override fun addToQueue(collection: DomainSongCollection) {
		viewModelScope.launch {
			val items = collection.songs.map { it.toMediaItem() }
			controller?.addMediaItems(items)
			_uiState.update { state ->
				val newQueue = state.queue + collection.songs
				state.copy(
					queue = newQueue,
					currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
					currentSong = if (state.currentIndex == -1) collection.songs.firstOrNull() else state.currentSong
				)
			}
		}
	}

	override fun removeFromQueue(index: Int) {
		viewModelScope.launch {
			controller?.removeMediaItem(index)
			_uiState.update { state ->
				val newQueue = state.queue.toMutableList().apply { removeAt(index) }
				val newIndex = when {
					index < state.currentIndex -> state.currentIndex - 1
					index == state.currentIndex -> if (newQueue.isEmpty()) -1 else state.currentIndex.coerceAtMost(
						newQueue.size - 1
					)

					else -> state.currentIndex
				}
				state.copy(
					queue = newQueue,
					currentIndex = newIndex,
					currentSong = if (newIndex == -1) null else newQueue[newIndex]
				)
			}
		}
	}

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
		viewModelScope.launch {
			controller?.moveMediaItem(fromIndex, toIndex)
			_uiState.update { state ->
				val newQueue = state.queue.toMutableList().apply {
					val item = removeAt(fromIndex)
					add(toIndex, item)
				}
				val newIndex = when (state.currentIndex) {
					fromIndex -> toIndex
					in (fromIndex + 1)..toIndex -> state.currentIndex - 1
					in toIndex until fromIndex -> state.currentIndex + 1
					else -> state.currentIndex
				}
				state.copy(
					queue = newQueue,
					currentIndex = newIndex,
					currentSong = if (newIndex == -1) null else newQueue[newIndex]
				)
			}
		}
	}

	override fun clearQueue() {
		viewModelScope.launch {
			controller?.clearMediaItems()
			_uiState.update {
				it.copy(
					queue = emptyList(),
					currentSong = null,
					currentIndex = -1,
					progress = 0f
				)
			}
		}
	}

	override fun playAt(index: Int) {
		viewModelScope.launch {
			controller?.let { player ->
				if (index in 0 until player.mediaItemCount) {
					player.seekTo(index, 0L)
					player.play()
				}
			}
		}
	}

	override fun playNextSingle(song: DomainSong) {
		viewModelScope.launch {
			controller?.addMediaItem(_uiState.value.currentIndex + 1, song.toMediaItem())
			_uiState.update { state ->
				val newQueue = 
					if (state.queue.isEmpty()) 
						state.queue + song
					else
						state.queue.slice(0..state.currentIndex) + song + state.queue.slice(state.currentIndex+1..state.queue.size-1)
				state.copy(
					queue = newQueue,
					currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
					currentSong = if (state.currentIndex == -1) song else state.currentSong
				)
			}
		}
	}

	override fun playNext(collection: DomainSongCollection) {
		viewModelScope.launch {
			val items = collection.songs.map { it.toMediaItem() }
			controller?.addMediaItems(_uiState.value.currentIndex + 1, items)
			_uiState.update { state ->
				val newQueue = 
					if (state.queue.isEmpty()) 
						state.queue + collection.songs
					else
						state.queue.slice(0..state.currentIndex) + collection.songs + state.queue.slice(state.currentIndex+1..state.queue.size-1)
				state.copy(
					queue = newQueue,
					currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
					currentSong = if (state.currentIndex == -1) collection.songs.firstOrNull() else state.currentSong
				)
			}
		}
	}

	override fun playRadio(radio: DomainRadio) {
		viewModelScope.launch {
			val radioId = "radio_${radio.name.hashCode()}"

			val dummyRadioSong = DomainSong(
				id = radioId,
				title = radio.name,
				artistName = "Live Radio",
				albumId = "radio_album",
				albumTitle = "Live Stream",
				duration = Duration.ZERO,
				trackNumber = 1,
				coverArtId = null,
				artistId = "",
				parentId = "",
				comment = null,
				discNumber = null,
				isrc = emptyList(),
				year = null,
				genre = null,
				genres = emptyList(),
				moods = emptyList(),
				bpm = null,
				contributors = emptyList(),
				playCount = 0,
				userRating = 0,
				averageRating = null,
				bitRate = null,
				bitDepth = null,
				sampleRate = null,
				audioChannelCount = null,
				replayGain = null,
				fileSize = 0,
				fileExtension = "",
				mimeType = "",
				filePath = radio.streamUrl,
				starredAt = null,
				musicBrainzId = null
			)

			val metadata = MediaMetadata.Builder()
				.setTitle(radio.name)
				.setArtist("Live Radio")
				.setIsPlayable(true)
				.build()

			val mediaItem = MediaItem.Builder()
				.setUri(radio.streamUrl)
				.setMediaId("radio_${radio.name.hashCode()}")
				.setMediaMetadata(metadata)
				.setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build())
				.build()

			controller?.let { player ->
				player.stop()
				player.clearMediaItems()
				player.setMediaItem(mediaItem)
				player.prepare()
				player.play()
			}

			_uiState.update { state ->
				state.copy(
					queue = listOf(dummyRadioSong),
					currentIndex = 0,
					currentSong = dummyRadioSong,
					isLoading = true
				)
			}
		}
	}

	override fun shufflePlay(collection: DomainSongCollection) {
		viewModelScope.launch {
			val shuffledSongs = collection.songs.shuffled()
			val mediaItems = shuffledSongs.map { it.toMediaItem() }

			controller?.let { player ->
				player.shuffleModeEnabled = false
				player.setMediaItems(mediaItems, 0, 0L)
				player.prepare()
				player.play()
			}

			_uiState.update { state ->
				state.copy(
					queue = shuffledSongs,
					currentIndex = 0,
					currentSong = shuffledSongs.firstOrNull()
				)
			}
		}
	}

	override fun pause() {
		viewModelScope.launch {
			controller?.pause()
		}
	}

	override fun resume() {
		viewModelScope.launch {
			controller?.play()
		}
	}

	override fun next() {
		viewModelScope.launch {
			if (controller?.hasNextMediaItem() == true) controller?.seekToNextMediaItem()
		}
	}

	override fun previous() {
		viewModelScope.launch {
			val controller = controller ?: return@launch
			if (controller.hasPreviousMediaItem() && controller.currentPosition <= 1000) {
				controller.seekToPreviousMediaItem()
			} else {
				controller.seekTo(0)
			}
		}
	}

	override fun toggleShuffle() {
		viewModelScope.launch {
			controller?.let { player ->
				player.shuffleModeEnabled = !player.shuffleModeEnabled
			}
		}
	}

	override fun toggleRepeat() {
		viewModelScope.launch {
			controller?.let { player ->
				player.repeatMode = when (player.repeatMode) {
					Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
					Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
					else -> Player.REPEAT_MODE_OFF
				}
			}
		}
	}

	override fun seek(normalized: Float) {
		viewModelScope.launch {
			controller?.let {
				val target = (it.duration * normalized).toLong()
				it.seekTo(target)
				_uiState.update { state ->
					state.copy(progress = normalized)
				}
			}
		}
	}

	override fun onCleared() {
		viewModelScope.launch {
			super.onCleared()
			controllerFuture?.let { MediaController.releaseFuture(it) }
		}
	}

	private fun DomainSong.toMediaItem(): MediaItem {
		val metadata = MediaMetadata.Builder()
			.setTitle(title)
			.setArtist(artistName)
			.setAlbumTitle(albumTitle)
			.setArtworkUri(
				coverArtId?.let {
					SessionManager.api.getCoverArtUrl(it, auth = true).toUri()
						.buildUpon()
						.appendQueryParameter("cacheKey", it)
						.build()
				}
			)
			.build()

		val uri = when {
			id.startsWith("radio_") && !filePath.isNullOrEmpty() -> { filePath.toUri() }
			else -> {
				val localPath = downloadManager.getDownloadedFilePath(id)
				if (localPath != null) {
					File(localPath).toUri()
				} else {
					getStreamUrl(id)
				}
			}
		}

		val builder = MediaItem.Builder()
			.setUri(uri)
			.setMediaId(id)
			.setMediaMetadata(metadata)

		if (id.startsWith("radio_")) {
			builder.setLiveConfiguration(MediaItem.LiveConfiguration.Builder().build())
		}

		return builder.build()
	}
}

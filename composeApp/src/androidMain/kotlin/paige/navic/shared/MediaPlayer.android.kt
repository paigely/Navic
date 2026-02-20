package paige.navic.shared

import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import paige.navic.MainActivity
import paige.navic.R
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection

class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null
	private val serviceScope = MainScope()
	private var scrobbleManager: AndroidScrobbleManager? = null

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
			.build().apply { setSmallIcon(R.drawable.ic_navic) }

		val player = ExoPlayer.Builder(this)
			.setLoadControl(loadControl)
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
			}

		scrobbleManager = AndroidScrobbleManager(player, serviceScope)

		val sessionIntent = Intent(this, MainActivity::class.java).apply {
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
			.build()
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return mediaSession
	}

	override fun onTaskRemoved(rootIntent: Intent?) {
		onDestroy()
		stopSelf()
	}

	override fun onDestroy() {
		scrobbleManager?.release()
		serviceScope.cancel()
		mediaSession?.run {
			player.release()
			release()
			mediaSession = null
		}
		super.onDestroy()
	}

	companion object {
		fun newSessionToken(context: Context): SessionToken {
			return SessionToken(context, ComponentName(context, PlaybackService::class.java))
		}
	}
}

class AndroidMediaPlayerViewModel(
	private val application: Application
) : MediaPlayerViewModel() {
	private var controller: MediaController? = null
	private var controllerFuture: ListenableFuture<MediaController>? = null

	private var loadingCollectionId: String? = null

	init {
		connectToService()
	}

	private fun connectToService() {
		val sessionToken = PlaybackService.newSessionToken(application)
		controllerFuture = MediaController.Builder(application, sessionToken).buildAsync()
		controllerFuture?.addListener({
			controller = controllerFuture?.get()
			setupController()
		}, MoreExecutors.directExecutor())
	}

	private fun setupController() {
		controller?.apply {
			addListener(object : Player.Listener {
				override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
					updatePlaybackState()
				}

				override fun onIsPlayingChanged(isPlaying: Boolean) {
					_uiState.update { it.copy(isPaused = !isPlaying) }
					if (isPlaying) startProgressLoop()
					val intent = Intent("${application.packageName}.NOW_PLAYING_UPDATED").apply {
						setPackage(application.packageName)
						putExtra("isPlaying", isPlaying)
						putExtra("title", _uiState.value.currentTrack?.title ?: "Unknown track")
						putExtra("artist", _uiState.value.currentTrack?.artist ?: "Unknown artist")
						putExtra("artUrl", SessionManager.api.getCoverArtUrl(
							id = _uiState.value.currentTrack?.coverArt, auth = true
						))
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
		}
	}

	private fun refreshCurrentCollection(albumId: String) {
		if (loadingCollectionId == albumId) return
			loadingCollectionId = albumId

		viewModelScope.launch {
			runCatching {
				val albumResponse = SessionManager.api.getAlbum(albumId)
				val album = albumResponse.data.album

				_uiState.update { it.copy(currentCollection = album) }
			}.onFailure {
				loadingCollectionId = null
			}
		}
	}

	private fun updatePlaybackState() {
		controller?.let { player ->
			val index = player.currentMediaItemIndex
			val currentTrack = _uiState.value.queue.getOrNull(index)

			val derivedCollection = currentTrack?.let { track ->
				val stateCollection = _uiState.value.currentCollection

				if (stateCollection?.id == track.albumId.toString()) {
					stateCollection
				} else {
					refreshCurrentCollection(track.albumId.toString())
					null
				}
			}

			_uiState.update { state ->
				state.copy(
					currentIndex = index,
					currentTrack = currentTrack,
					currentCollection = derivedCollection ?: state.currentCollection,
					isPaused = !player.isPlaying,
					isShuffleEnabled = player.shuffleModeEnabled,
					repeatMode = player.repeatMode
				)
			}
			updateProgress()
		}
	}

	private fun startProgressLoop() {
		viewModelScope.launch {
			while (controller?.isPlaying == true) {
				val player = controller ?: break
				val duration = player.duration.coerceAtLeast(1)
				val progress = (player.currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
				_uiState.update { it.copy(progress = progress) }
				delay(200)
			}
		}
	}

	private fun updateProgress() {
		controller?.let { player ->
			val duration = player.duration.coerceAtLeast(1)
			val pos = player.currentPosition
			val progress = (pos.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
			_uiState.update { it.copy(progress = progress) }
		}
	}

	override fun addToQueueSingle(track: Track) {
		controller?.addMediaItem(track.toMediaItem(true))
		_uiState.update { it.copy(queue = it.queue + track) }
	}

	override fun addToQueue(tracks: TrackCollection) {
		val items = tracks.tracks.map { it.toMediaItem(false) }
		controller?.addMediaItems(items)
		_uiState.update { it.copy(queue = it.queue + tracks.tracks) }
	}

	override fun removeFromQueue(index: Int) {
		controller?.removeMediaItem(index)
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply { removeAt(index) }
			state.copy(queue = newQueue)
		}
	}

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
		controller?.moveMediaItem(fromIndex, toIndex)
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply {
				val item = removeAt(fromIndex)
				add(toIndex, item)
			}
			state.copy(queue = newQueue)
		}
	}

	override fun clearQueue() {
		controller?.clearMediaItems()
		_uiState.update { it.copy(queue = emptyList(), currentTrack = null, currentIndex = -1) }
	}

	override fun playAt(index: Int) {
		controller?.let { player ->
			if (index in 0 until player.mediaItemCount) {
				player.seekTo(index, 0L)
				player.play()
			}
		}
	}

	override fun shufflePlay(tracks: TrackCollection) {
		val shuffledTracks = tracks.tracks.shuffled()
		val mediaItems = shuffledTracks.map { it.toMediaItem(false) }

		controller?.let { player ->
			player.shuffleModeEnabled = false
			player.setMediaItems(mediaItems, 0, 0L)
			player.prepare()
			player.play()
		}

		_uiState.update { it.copy(queue = shuffledTracks) }
	}

	override fun pause() { controller?.pause() }
	override fun resume() { controller?.play() }
	override fun next() { if (controller?.hasNextMediaItem() == true) controller?.seekToNextMediaItem() }
	override fun previous() { if (controller?.hasPreviousMediaItem() == true) controller?.seekToPreviousMediaItem() }
	override fun toggleShuffle() {
		controller?.let { player ->
			player.shuffleModeEnabled = !player.shuffleModeEnabled
		}
	}
	override fun toggleRepeat() {
		controller?.let { player ->
			player.repeatMode = when (player.repeatMode) {
				Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
				else -> Player.REPEAT_MODE_OFF
			}
		}
	}

	override fun seek(normalized: Float) {
		controller?.let {
			val target = (it.duration * normalized).toLong()
			it.seekTo(target)
			_uiState.update { state ->
				state.copy(progress = normalized)
			}
		}
	}

	override fun onCleared() {
		super.onCleared()
		controllerFuture?.let { MediaController.releaseFuture(it) }
	}

	private fun Track.toMediaItem(single: Boolean): MediaItem {
		if (single) {
			val metadata = MediaMetadata.Builder()
				.setTitle(title)
				.setArtist(artist)
				.setAlbumTitle(album)
				.setArtworkUri(coverArt?.toUri())
				.build()

			return MediaItem.Builder()
				.setUri(SessionManager.api.streamUrl(id))
				.setMediaId(id)
				.setMediaMetadata(metadata)
				.build()
		} else {
			val metadata = MediaMetadata.Builder()
				.setTitle(title)
				.setArtist(artist)
				.setAlbumTitle(album)
				.setArtworkUri(SessionManager.api.getCoverArtUrl(coverArt, auth = true)?.toUri())
				.build()

			return MediaItem.Builder()
				.setUri(SessionManager.api.streamUrl(id))
				.setMediaId(id)
				.setMediaMetadata(metadata)
				.build()
		}
	}
}

@Composable
actual fun rememberMediaPlayer(): MediaPlayerViewModel {
	val context = LocalContext.current.applicationContext as Application
	return viewModel { AndroidMediaPlayerViewModel(context) }
}

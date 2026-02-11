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
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.MainActivity
import paige.navic.data.models.Settings
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection

class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null

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
		val player = ExoPlayer.Builder(this)
			.setLoadControl(loadControl)
			.build()
			.apply {
				setAudioAttributes(
					androidx.media3.common.AudioAttributes.Builder()
						.setUsage(androidx.media3.common.C.USAGE_MEDIA)
						.setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
						.build(),
					true
				)
			}
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
							id = _uiState.value.currentTrack?.coverArt, auth = true, size = 700
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

	private fun updatePlaybackState() {
		controller?.let { player ->
			val index = player.currentMediaItemIndex
			val currentTrack = player.currentMediaItem
			val previousTrack = _uiState.value.currentTrack

			if (currentTrack?.mediaId != previousTrack?.id) {
				scrobbleNowPlaying(currentTrack?.mediaId)
				if (_uiState.value.progress >= Settings.shared.scrobblePercentage
					&& (_uiState.value.currentTrack?.duration?.toFloat()
						?: Settings.shared.minDurationToScrobble) >= Settings.shared.minDurationToScrobble) {
					scrobbleSubmission(previousTrack?.id)
				}
			}

			_uiState.update { state ->
				state.copy(
					currentIndex = index,
					currentTrack = state.tracks?.tracks?.getOrNull(index),
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
				updateProgress()
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

	override fun play(tracks: TrackCollection, startIndex: Int) {
		_uiState.update { it.copy(tracks = tracks, isLoading = true) }

		viewModelScope.launch(Dispatchers.IO) {
			val mediaItems = tracks.tracks.map { track ->
				val metadata = MediaMetadata.Builder()
					.setTitle(track.title)
					.setArtist(track.artist)
					.setAlbumTitle(track.album)
					.setArtworkUri(SessionManager.api.getCoverArtUrl(track.coverArt, auth = true)?.toUri())
					.build()

				MediaItem.Builder()
					.setUri(SessionManager.api.streamUrl(track.id))
					.setMediaId(track.id)
					.setMediaMetadata(metadata)
					.build()
			}

			withContext(Dispatchers.Main) {
				controller?.let { player ->
					player.setMediaItems(mediaItems, startIndex, 0L)
					player.prepare()
					player.play()
				}
			}
		}
	}

	override fun playSingle(track: Track) {
		viewModelScope.launch {
			runCatching {
				val albumResponse = SessionManager.api.getAlbum(track.albumId.toString())
				val album = albumResponse.data.album
				val index = album.tracks.indexOfFirst { it.id == track.id }
				if (index != -1) {
					play(album, index)
				}
			}
		}
	}

	override fun shufflePlay(tracks: TrackCollection) {
		controller?.shuffleModeEnabled = true
		play(tracks, 0)
	}

	override fun toggleRepeat() {
		controller?.let { player ->
			player.repeatMode = when (player.repeatMode) {
				Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
				else -> Player.REPEAT_MODE_OFF
			}
		}
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
}

@Composable
actual fun rememberMediaPlayer(): MediaPlayerViewModel {
	val context = LocalContext.current.applicationContext as Application
	return viewModel { AndroidMediaPlayerViewModel(context) }
}

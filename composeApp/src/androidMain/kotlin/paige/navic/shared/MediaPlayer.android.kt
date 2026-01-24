package paige.navic.shared

import android.app.Application
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
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
import paige.navic.data.model.Settings
import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Track
import paige.subsonic.api.model.TrackCollection

class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null

	override fun onCreate() {
		super.onCreate()
		val player = ExoPlayer.Builder(this).build().apply {
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

	// todo: actually handle restoring state
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
				}

				override fun onPlaybackStateChanged(playbackState: Int) {
					_uiState.update { it.copy(isLoading = playbackState == Player.STATE_BUFFERING) }
					updatePlaybackState()
				}
			})
			updatePlaybackState()
		}
	}

	private fun updatePlaybackState() {
		controller?.let { player ->
			val index = player.currentMediaItemIndex
			val oldIndex = _uiState.value.currentIndex

			if (index != oldIndex) {
				scrobbleNowPlaying(index)
				if (_uiState.value.progress >= Settings.shared.scrobblePercentage
					&& (_uiState.value.currentTrack?.duration?.toFloat()
						?: Settings.shared.minDurationToScrobble) >= Settings.shared.minDurationToScrobble) {
					scrobbleSubmission(oldIndex)
				}
			}

			_uiState.update { state ->
				state.copy(
					currentIndex = index,
					currentTrack = state.tracks?.tracks?.getOrNull(index),
					isPaused = !player.isPlaying
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
				val url = try {
					SessionManager.api.streamUrl(track.id)
				} catch (_: Exception) {
					""
				}

				val metadata = MediaMetadata.Builder()
					.setTitle(track.title)
					.setArtist(track.artist)
					.setAlbumTitle(track.album)
					.setArtworkUri(SessionManager.api.getCoverArtUrl(
						track.coverArt, auth = true
					)?.toUri())
					.build()

				MediaItem.Builder()
					.setUri(url)
					.setMediaId(track.id)
					.setMediaMetadata(metadata)
					.build()
			}

			withContext(Dispatchers.Main) {
				controller?.setMediaItems(mediaItems, startIndex, 0L)
				controller?.prepare()
				controller?.play()
			}
		}
	}

	override fun playSingle(track: Track) {
		viewModelScope.launch {
			_uiState.update {
				it.copy(
					currentTrack = track,
					isLoading = true
				)
			}

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

	override fun pause() { controller?.pause() }
	override fun resume() { controller?.play() }
	override fun next() { if (controller?.hasNextMediaItem() == true) controller?.seekToNextMediaItem() }
	override fun previous() { if (controller?.hasPreviousMediaItem() == true) controller?.seekToPreviousMediaItem() }

	override fun seek(normalized: Float) {
		controller?.let {
			val target = (it.duration * normalized).toLong()
			it.seekTo(target)
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

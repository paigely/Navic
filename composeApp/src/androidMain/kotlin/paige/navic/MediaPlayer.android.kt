package paige.navic

import android.content.ComponentName
import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.AnyTracks
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import kotlinx.coroutines.delay

class PlaybackService : MediaSessionService() {
	private var mediaSession: MediaSession? = null

	companion object {
		fun newSessionToken(context: Context): SessionToken {
			return SessionToken(context, ComponentName(context, PlaybackService::class.java))
		}
	}

	override fun onCreate() {
		super.onCreate()
		val player = ExoPlayer.Builder(this).build()
		mediaSession = MediaSession.Builder(this, player).build()
	}

	override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
		return mediaSession
	}

	override fun onDestroy() {
		mediaSession?.run {
			player.release()
			release()
			mediaSession = null
		}
		super.onDestroy()
	}
}

@OptIn(UnstableApi::class)
private class MediaPlayerImpl(
	private val scope: CoroutineScope,
	private val context: Context
) : MediaPlayer {

	private var controllerFuture: ListenableFuture<MediaController>? = null
	private var controller: MediaController? = null

	private val _tracks = mutableStateOf<AnyTracks?>(null)
	override var tracks: AnyTracks?
		get() = _tracks.value
		set(value) { _tracks.value = value }

	private val _progress = mutableFloatStateOf(0f)
	override val progress: State<Float> = _progress

	private val _currentIndex = mutableIntStateOf(-1)
	override val currentIndex: State<Int> = _currentIndex

	private val _isPaused = mutableStateOf(false)
	override val isPaused: State<Boolean> = _isPaused

	fun connect() {
		val sessionToken = PlaybackService.newSessionToken(context)
		controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
		controllerFuture?.addListener({
			controller = controllerFuture?.get()
			setupPlayerListeners()
			syncState()
		}, MoreExecutors.directExecutor())
	}

	fun disconnect() {
		controllerFuture?.let { MediaController.releaseFuture(it) }
		controller = null
	}

	private fun setupPlayerListeners() {
		controller?.addListener(object : Player.Listener {
			override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
				updateCurrentIndex()
			}

			override fun onIsPlayingChanged(isPlaying: Boolean) {
				_isPaused.value = !isPlaying
				if (isPlaying) startProgressLoop()
			}

			override fun onPlaybackStateChanged(playbackState: Int) {
				updateProgress()
			}
		})
	}

	private fun syncState() {
		controller?.let {
			_isPaused.value = !it.isPlaying
			updateCurrentIndex()
			updateProgress()
			if (it.isPlaying) startProgressLoop()
		}
	}

	private fun startProgressLoop() {
		scope.launch {
			while (controller?.isPlaying == true) {
				updateProgress()
				delay(200)
			}
		}
	}

	private fun updateProgress() {
		controller?.let {
			val duration = it.duration.coerceAtLeast(1)
			val position = it.currentPosition
			_progress.floatValue = (position.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
		}
	}

	private fun updateCurrentIndex() {
		controller?.let {
			_currentIndex.intValue = it.currentMediaItemIndex
		}
	}

	override fun play(tracks: AnyTracks, songIndex: Int) {
		this.tracks = tracks

		scope.launch(Dispatchers.IO) {
			val mediaItems = tracks.tracks.map { track ->
				val url = try {
					SessionManager.api.streamUrl(track.id)
				} catch (e: Exception) {
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
				controller?.run {
					setMediaItems(mediaItems, songIndex, 0L)
					prepare()
					play()
				}
			}
		}
	}

	override fun pause() {
		controller?.pause()
	}

	override fun resume() {
		controller?.play()
	}

	override fun seek(normalized: Float) {
		controller?.let {
			val duration = it.duration
			if (duration > 0) {
				it.seekTo((duration * normalized).toLong())
				updateProgress()
			}
		}
	}

	override fun next() {
		if (controller?.hasNextMediaItem() == true) {
			controller?.seekToNextMediaItem()
		}
	}

	override fun previous() {
		if (controller?.hasPreviousMediaItem() == true) {
			controller?.seekToPreviousMediaItem()
		}
	}
}

@Composable
actual fun rememberMediaPlayer(): MediaPlayer {
	val context = LocalContext.current
	val scope = rememberCoroutineScope()

	val mediaPlayer = remember { MediaPlayerImpl(scope, context) }

	DisposableEffect(context) {
		mediaPlayer.connect()
		onDispose {
			mediaPlayer.disconnect()
		}
	}

	return mediaPlayer
}

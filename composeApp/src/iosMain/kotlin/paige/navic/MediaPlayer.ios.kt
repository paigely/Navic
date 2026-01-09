package paige.navic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.AnyTrack
import paige.subsonic.api.model.AnyTracks
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.setActive
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification
import platform.AVFoundation.addPeriodicTimeObserverForInterval
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVFoundation.removeTimeObserver
import platform.AVFoundation.replaceCurrentItemWithPlayerItem
import platform.AVFoundation.seekToTime
import platform.CoreGraphics.CGSizeMake
import platform.CoreMedia.CMTimeGetSeconds
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeMakeWithSeconds
import platform.Foundation.NSData
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.MediaPlayer.MPChangePlaybackPositionCommandEvent
import platform.MediaPlayer.MPMediaItemArtwork
import platform.MediaPlayer.MPMediaItemPropertyAlbumTitle
import platform.MediaPlayer.MPMediaItemPropertyArtist
import platform.MediaPlayer.MPMediaItemPropertyArtwork
import platform.MediaPlayer.MPMediaItemPropertyPlaybackDuration
import platform.MediaPlayer.MPMediaItemPropertyTitle
import platform.MediaPlayer.MPNowPlayingInfoCenter
import platform.MediaPlayer.MPNowPlayingInfoPropertyElapsedPlaybackTime
import platform.MediaPlayer.MPNowPlayingInfoPropertyPlaybackRate
import platform.MediaPlayer.MPRemoteCommandCenter
import platform.MediaPlayer.MPRemoteCommandHandlerStatusCommandFailed
import platform.MediaPlayer.MPRemoteCommandHandlerStatusSuccess
import platform.UIKit.UIImage
import kotlin.time.Clock

@Composable
actual fun rememberMediaPlayer(): MediaPlayer {
	val scope = rememberCoroutineScope()
	val mediaPlayer = remember { IOSMediaPlayer(scope) }

	DisposableEffect(Unit) {
		onDispose {
			mediaPlayer.cleanup()
		}
	}
	return mediaPlayer
}

@OptIn(
	ExperimentalForeignApi::class,
	BetaInteropApi::class
)
class IOSMediaPlayer(
	private val scope: CoroutineScope
) : MediaPlayer {

	private val player = AVPlayer()

	private var playlist: List<AnyTrack> = emptyList()
	private var currentSongIndex = 0
	private var preparedUrls: List<String> = emptyList()

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

	private var timeObserver: Any? = null

	init {
		setupAudioSession()
		setupRemoteCommands()
		startProgressObserver()

		NSNotificationCenter.defaultCenter.addObserverForName(
			name = AVPlayerItemDidPlayToEndTimeNotification,
			`object` = null,
			queue = NSOperationQueue.mainQueue
		) { _ ->
			next()
		}
	}

	private fun setupAudioSession() {
		val audioSession = AVAudioSession.sharedInstance()
		try {
			audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
			audioSession.setActive(true, error = null)
		} catch (e: Exception) {
			println("failed to setup audio session ${e.message}")
		}
	}

	private fun setupRemoteCommands() {
		val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()

		commandCenter.playCommand.addTargetWithHandler {
			resume()
			MPRemoteCommandHandlerStatusSuccess
		}

		commandCenter.pauseCommand.addTargetWithHandler {
			pause()
			MPRemoteCommandHandlerStatusSuccess
		}

		commandCenter.nextTrackCommand.addTargetWithHandler {
			next()
			MPRemoteCommandHandlerStatusSuccess
		}

		commandCenter.previousTrackCommand.addTargetWithHandler {
			previous()
			MPRemoteCommandHandlerStatusSuccess
		}

		commandCenter.changePlaybackPositionCommand.addTargetWithHandler { event ->
			val positionEvent = event as? MPChangePlaybackPositionCommandEvent
			if (positionEvent != null) {
				seekToTime(positionEvent.positionTime)
				MPRemoteCommandHandlerStatusSuccess
			} else {
				MPRemoteCommandHandlerStatusCommandFailed
			}
		}
	}

	override fun play(tracks: AnyTracks, songIndex: Int) {
		this.tracks = tracks
		this.playlist = tracks.tracks

		scope.launch {
			val urls = withContext(Dispatchers.Default) {
				tracks.tracks.map { track ->
					try {
						SessionManager.api.streamUrl(track.id)
					} catch (e: Exception) {
						""
					}
				}
			}

			withContext(Dispatchers.Main) {
				preparedUrls = urls
				playIndex(songIndex)
			}
		}
	}

	private fun playIndex(index: Int) {
		if (index !in playlist.indices || index !in preparedUrls.indices) return

		scope.launch {
			if (currentSongIndex != index) {
				tracks?.tracks?.getOrNull(currentSongIndex)?.let { track ->
					try {
						SessionManager.api.scrobble(
							track.id,
							Clock.System.now()
								.toEpochMilliseconds(),
							submission = true
						)
					} catch (e: Exception) {
						println(e)
					}
				}
			}
			tracks?.tracks?.getOrNull(index)?.let { track ->
				try {
					SessionManager.api.scrobble(
						track.id,
						Clock.System.now()
							.toEpochMilliseconds(),
						submission = false
					)
				} catch(e: Exception) {
					println(e)
				}
			}
		}

		currentSongIndex = index
		_currentIndex.intValue = index

		val urlStr = preparedUrls[index]
		if (urlStr.isEmpty()) {
			next()
			return
		}

		val url = NSURL.URLWithString(urlStr)
		val playerItem = AVPlayerItem(url!!)

		player.replaceCurrentItemWithPlayerItem(playerItem)
		player.play()

		_isPaused.value = false
		updateNowPlayingInfo(playlist[index])
	}

	@ObjCAction
	override fun resume() {
		player.play()
		_isPaused.value = false
		updateNowPlayingInfo(playlist.getOrNull(currentSongIndex))
	}

	@ObjCAction
	override fun pause() {
		player.pause()
		_isPaused.value = true
		updateNowPlayingInfo(playlist.getOrNull(currentSongIndex))
	}

	@ObjCAction
	override fun next() {
		if (currentSongIndex + 1 < playlist.size) {
			playIndex(currentSongIndex + 1)
		}
	}

	@ObjCAction
	override fun previous() {
		if (currentSongIndex - 1 >= 0) {
			playIndex(currentSongIndex - 1)
		}
	}

	override fun seek(normalized: Float) {
		val duration = player.currentItem?.duration
		if (duration != null) {
			val totalSeconds = CMTimeGetSeconds(duration)
			if (!totalSeconds.isNaN()) {
				val targetTime = totalSeconds * normalized
				seekToTime(targetTime)
			}
		}
	}

	private fun seekToTime(seconds: Double) {
		val cmTime = CMTimeMakeWithSeconds(seconds, preferredTimescale = 1000)
		player.seekToTime(cmTime)
	}

	private fun startProgressObserver() {
		val interval = CMTimeMake(value = 1, timescale = 5)
		timeObserver = player.addPeriodicTimeObserverForInterval(interval, queue = null) { time ->
			val duration = player.currentItem?.duration
			if (duration != null) {
				val totalSeconds = CMTimeGetSeconds(duration)
				val currentSeconds = CMTimeGetSeconds(time)
				if (!totalSeconds.isNaN() && totalSeconds > 0) {
					_progress.floatValue = (currentSeconds / totalSeconds).toFloat()
				}
			}
		}
	}

	@OptIn(ExperimentalForeignApi::class)
	private fun updateNowPlayingInfo(track: AnyTrack?) {
		if (track == null) {
			MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
			return
		}

		val info = mutableMapOf<Any?, Any?>()
		info[MPMediaItemPropertyTitle] = track.title
		info[MPMediaItemPropertyArtist] = track.artist ?: ""
		info[MPMediaItemPropertyAlbumTitle] = track.album ?: ""

		val duration = player.currentItem?.duration
		if (duration != null) {
			val seconds = CMTimeGetSeconds(duration)
			if (!seconds.isNaN()) {
				info[MPMediaItemPropertyPlaybackDuration] = seconds
			}
		}

		info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(player.currentTime())
		info[MPNowPlayingInfoPropertyPlaybackRate] = if (_isPaused.value) 0.0 else 1.0

		info[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(
			boundsSize = CGSizeMake(512.0, 512.0),
			requestHandler = {
				return@MPMediaItemArtwork track.coverArt
					?.let { SessionManager.api.getCoverArtUrl(it, auth = true) }
					?.let { NSURL.URLWithString(it) }
					?.let { NSData.dataWithContentsOfURL(it) }
					?.let { UIImage(data = it) } ?: UIImage()
			}
		)

		MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = info
	}

	fun cleanup() {
		timeObserver?.let { player.removeTimeObserver(it) }
		timeObserver = null
		player.replaceCurrentItemWithPlayerItem(null)
	}
}

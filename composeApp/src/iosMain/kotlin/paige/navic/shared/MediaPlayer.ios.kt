package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import paige.navic.data.models.Settings
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection
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

@OptIn(ExperimentalForeignApi::class)
class IOSMediaPlayerViewModel : MediaPlayerViewModel() {
	private val player = AVPlayer()
	private var timeObserver: Any? = null
	private var preparedUrls: List<String> = emptyList()
	private var lastScrobbledTrackId: String? = null
	private var lastTrackForSubmission: Track? = null

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

	override fun play(tracks: TrackCollection, startIndex: Int) {
		_uiState.update { it.copy(tracks = tracks, isLoading = true) }

		viewModelScope.launch(Dispatchers.Default) {
			val urls = tracks.tracks.map { track ->
				try { SessionManager.api.streamUrl(track.id) } catch (e: Exception) { "" }
			}

			withContext(Dispatchers.Main) {
				preparedUrls = urls
				playIndex(startIndex)
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

	private fun playIndex(index: Int) {
		val tracks = _uiState.value.tracks?.tracks ?: return
		if (index !in tracks.indices || index !in preparedUrls.indices) return

		val trackToPlay = tracks[index]

		if (trackToPlay.id != lastScrobbledTrackId) {
			lastTrackForSubmission?.takeIf {
				it.duration?.toFloat()!! >= Settings.shared.minDurationToScrobble &&
					_uiState.value.progress >= Settings.shared.scrobblePercentage
			}?.let {
				scrobbleSubmission(it.id)
			}

			scrobbleNowPlaying(trackToPlay.id)

			// Update guards
			lastScrobbledTrackId = trackToPlay.id
			lastTrackForSubmission = trackToPlay
		}

		val urlStr = preparedUrls[index]
		if (urlStr.isEmpty()) {
			if (index < tracks.size - 1) playIndex(index + 1)
			return
		}

		val playerItem = AVPlayerItem(NSURL.URLWithString(urlStr)!!)
		player.replaceCurrentItemWithPlayerItem(playerItem)
		player.play()

		_uiState.update {
			it.copy(
				currentIndex = index,
				currentTrack = trackToPlay,
				isPaused = false,
				isLoading = false
			)
		}

		updateNowPlayingInfo(trackToPlay)
	}

	override fun resume() {
		player.play()
		_uiState.update { it.copy(isPaused = false) }
		updateNowPlayingInfo(_uiState.value.currentTrack)
	}

	override fun pause() {
		player.pause()
		_uiState.update { it.copy(isPaused = true) }
		updateNowPlayingInfo(_uiState.value.currentTrack)
	}

	override fun next() {
		val nextIdx = _uiState.value.currentIndex + 1
		if (nextIdx < (_uiState.value.tracks?.tracks?.size ?: 0)) {
			playIndex(nextIdx)
		}
	}

	override fun previous() {
		val prevIdx = _uiState.value.currentIndex - 1
		if (prevIdx >= 0) {
			playIndex(prevIdx)
		}
	}

	override fun toggleShuffle() {
		// todo: Not yet implemented
	}

	override fun toggleRepeat() {
		// todo: Not yet implemented
	}

	override fun shufflePlay(tracks: TrackCollection) {
		// todo: Not yet implemented
	}

	override fun seek(normalized: Float) {
		val duration = player.currentItem?.duration ?: return
		val totalSeconds = CMTimeGetSeconds(duration)
		if (!totalSeconds.isNaN()) {
			val targetTime = CMTimeMakeWithSeconds(totalSeconds * normalized, 1000)
			player.seekToTime(targetTime)
		}
	}

	private fun seekToTime(seconds: Double) {
		val cmTime = CMTimeMakeWithSeconds(seconds, preferredTimescale = 1000)
		player.seekToTime(cmTime)
	}

	private fun startProgressObserver() {
		val interval = CMTimeMake(1, 20)
		timeObserver = player.addPeriodicTimeObserverForInterval(interval, null) { time ->
			val duration = player.currentItem?.duration
			if (duration != null) {
				val total = CMTimeGetSeconds(duration)
				val current = CMTimeGetSeconds(time)
				if (!total.isNaN() && total > 0) {
					_uiState.update { it.copy(progress = (current / total).toFloat()) }
				}
			}
		}
	}

	private fun updateNowPlayingInfo(track: Track?) {
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
		info[MPNowPlayingInfoPropertyPlaybackRate] = if (_uiState.value.isPaused) 0.0 else 1.0

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

	override fun onCleared() {
		super.onCleared()
		timeObserver?.let { player.removeTimeObserver(it) }
		player.replaceCurrentItemWithPlayerItem(null)
	}
}

@Composable
actual fun rememberMediaPlayer(): MediaPlayerViewModel {
	return viewModel { IOSMediaPlayerViewModel() }
}

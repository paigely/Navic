@file:OptIn(ExperimentalForeignApi::class)

package paige.navic.shared

import androidx.lifecycle.viewModelScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.update
import paige.navic.domain.repositories.TrackRepository
import paige.navic.domain.models.DomainSongCollection
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager
import paige.navic.managers.IOSScrobbleManager
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

class IOSMediaPlayerViewModel(
	stateRepository: PlayerStateRepository,
	trackRepository: TrackRepository,
	downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : MediaPlayerViewModel(
	stateRepository = stateRepository,
	trackRepository = trackRepository,
	downloadManager = downloadManager,
	connectivityManager = connectivityManager
) {
	private val player = AVPlayer()
	private var timeObserver: Any? = null
	private val scrobbleManager = IOSScrobbleManager(player, viewModelScope)
	private var pendingSyncState: PlayerUiState? = null

	init {
		setupAudioSession()
		setupRemoteCommands()
		startProgressObserver()

		NSNotificationCenter.defaultCenter.addObserverForName(
			name = AVPlayerItemDidPlayToEndTimeNotification,
			`object` = null,
			queue = NSOperationQueue.mainQueue
		) { _ ->
			when (_uiState.value.repeatMode) {
				1 -> {
					seek(0f); resume()
				}

				else -> next()
			}
		}

		pendingSyncState?.let { state ->
			syncPlayerWithState(state)
			pendingSyncState = null
		}
	}

	private fun setupAudioSession() {
		val audioSession = AVAudioSession.sharedInstance()
		try {
			audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
			audioSession.setActive(true, error = null)
		} catch (e: Exception) {
			Logger.e("IOSMediaPlayerViewModel", "Failed to setup audio session!", e)
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

	override fun playAt(index: Int) {
		val trackToPlay = _uiState.value.queue.getOrNull(index) ?: return

		if (!isAvailable(trackToPlay.id)) {
			next()
			return
		}

		val localPath = downloadManager.getDownloadedFilePath(trackToPlay.id)
		val url = if (localPath != null) {
			NSURL.fileURLWithPath(localPath)
		} else {
			NSURL.URLWithString(SessionManager.api.getStreamUrl(trackToPlay.id))!!
		}

		player.replaceCurrentItemWithPlayerItem(
			AVPlayerItem(url)
		)
		player.play()

		_uiState.update {
			it.copy(
				currentIndex = index,
				currentTrack = trackToPlay,
				isPaused = false,
				isLoading = false
			)
		}

		scrobbleManager.onMediaChanged(trackToPlay.id)
		scrobbleManager.onIsPlayingChanged(true)
		updateNowPlayingInfo(trackToPlay)
	}

	override fun addToQueueSingle(track: DomainSong) {
		_uiState.update { state ->
			val newQueue = state.queue + track
			val newIndex = newQueue.indexOf(state.currentTrack)
			state.copy(
				queue = newQueue,
				currentIndex = newIndex,
				currentTrack = if (newIndex == -1) null else state.currentTrack
			)
		}
	}

	override fun addToQueue(tracks: DomainSongCollection) {
		_uiState.update { state ->
			val newQueue = state.queue + tracks.songs
			val newIndex = newQueue.indexOf(state.currentTrack)
			state.copy(
				queue = newQueue,
				currentIndex = newIndex,
				currentTrack = if (newIndex == -1) null else state.currentTrack
			)
		}
	}

	override fun removeFromQueue(index: Int) {
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply {
				if (index in indices) removeAt(index)
			}
			state.copy(
				queue = newQueue,
				currentIndex = newQueue.indexOf(state.currentTrack),
				currentTrack = if (newQueue.indexOf(state.currentTrack) == -1) null else state.currentTrack
			)
		}
	}

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply {
				if (fromIndex in indices && toIndex in 0..size) {
					val item = removeAt(fromIndex)
					add(toIndex, item)
				}
			}
			state.copy(
				queue = newQueue,
				currentIndex = newQueue.indexOf(state.currentTrack),
				currentTrack = if (newQueue.indexOf(state.currentTrack) == -1) null else state.currentTrack
			)
		}
	}

	override fun clearQueue() {
		player.replaceCurrentItemWithPlayerItem(null)
		_uiState.update {
			it.copy(queue = emptyList(), currentTrack = null, currentIndex = -1, progress = 0f)
		}
		scrobbleManager.onIsPlayingChanged(false)
		updateNowPlayingInfo(null)
	}

	override fun resume() {
		player.play()
		_uiState.update { it.copy(isPaused = false) }
		scrobbleManager.onIsPlayingChanged(true)
		updateNowPlayingInfo(_uiState.value.currentTrack)
	}

	override fun pause() {
		player.pause()
		_uiState.update { it.copy(isPaused = true) }
		scrobbleManager.onIsPlayingChanged(false)
		updateNowPlayingInfo(_uiState.value.currentTrack)
	}

	override fun next() {
		if (_uiState.value.currentIndex + 1 < _uiState.value.queue.size) {
			playAt(_uiState.value.currentIndex + 1)
		}
	}

	override fun previous() {
		if ((_uiState.value.currentIndex - 1) >= 0) {
			playAt(_uiState.value.currentIndex - 1)
		} else {
			seek(0f)
		}
	}

	override fun toggleShuffle() {
		_uiState.update { it.copy(isShuffleEnabled = !it.isShuffleEnabled) }
	}

	override fun toggleRepeat() {
		_uiState.update {
			it.copy(repeatMode = if (it.repeatMode == 0) 1 else 0)
		}
	}

	override fun shufflePlay(tracks: DomainSongCollection) {
		val shuffledTracks = tracks.songs.shuffled()
		_uiState.update { state ->
			val newIndex = shuffledTracks.indexOf(state.currentTrack)
			state.copy(
				queue = shuffledTracks,
				currentIndex = newIndex,
				currentTrack = if (newIndex == -1) null else state.currentTrack
			)
		}
		playAt(0)
	}

	override fun seek(normalized: Float) {
		val duration = player.currentItem?.duration ?: return
		val totalSeconds = CMTimeGetSeconds(duration)
		if (!totalSeconds.isNaN()) {
			seekToTime(totalSeconds * normalized)
			_uiState.update { it.copy(progress = normalized) }
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

	private fun updateNowPlayingInfo(track: DomainSong?) {
		if (track == null) {
			MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
			return
		}

		val info = mutableMapOf<Any?, Any?>()
		info[MPMediaItemPropertyTitle] = track.title
		info[MPMediaItemPropertyArtist] = track.artistName
		info[MPMediaItemPropertyAlbumTitle] = track.albumTitle

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
				return@MPMediaItemArtwork track.coverArtId
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

	override fun syncPlayerWithState(state: PlayerUiState) {
		val track = state.queue.getOrNull(state.currentIndex) ?: return
		val url = NSURL.URLWithString(SessionManager.api.getStreamUrl(track.id)) ?: return
		player.replaceCurrentItemWithPlayerItem(AVPlayerItem(url))
		updateNowPlayingInfo(track)
	}
}

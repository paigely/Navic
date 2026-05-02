@file:OptIn(ExperimentalForeignApi::class)

package paige.navic.shared

import androidx.lifecycle.viewModelScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.flow.update
import paige.navic.data.database.SyncManager
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainRadio
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
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
import platform.AVFoundation.setRate
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
	downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager,
	syncManager: SyncManager
) : MediaPlayerViewModel(
	stateRepository = stateRepository,
	downloadManager = downloadManager,
	connectivityManager = connectivityManager
) {
	private val player = AVPlayer()
	private var timeObserver: Any? = null
	private val scrobbleManager = IOSScrobbleManager(player, viewModelScope, connectivityManager, syncManager)
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
		val songToPlay = _uiState.value.queue.getOrNull(index) ?: return

		if (!songToPlay.id.startsWith("radio_") && !isAvailable(songToPlay.id)) {
			next()
			return
		}

		val url = getSongUrl(songToPlay) ?: return

		player.replaceCurrentItemWithPlayerItem(AVPlayerItem(url))
		player.play()

		_uiState.update {
			it.copy(
				currentIndex = index,
				currentSong = songToPlay,
				isPaused = false,
				isLoading = false
			)
		}

		scrobbleManager.onMediaChanged(songToPlay.id)
		scrobbleManager.onIsPlayingChanged(true)
		updateNowPlayingInfo(songToPlay)
	}

	override fun playNextSingle(song: DomainSong) {
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

	override fun playNext(collection: DomainSongCollection) {
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

	override fun playRadio(radio: DomainRadio) {
		val radioId = "radio_${radio.name.hashCode()}"

		val dummyRadioSong = DomainSong(
			id = radioId,
			title = radio.name,
			artistName = "Live Radio",
			albumId = "radio_album",
			albumTitle = "Live Stream",
			duration = kotlin.time.Duration.ZERO,
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
			musicBrainzId = null,
			explicitStatus = DomainExplicitStatus.Unknown
		)

		val url = NSURL.URLWithString(radio.streamUrl)
		if (url != null) {
			player.replaceCurrentItemWithPlayerItem(AVPlayerItem(url))
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

		scrobbleManager.onMediaChanged(radioId)
		scrobbleManager.onIsPlayingChanged(true)
		updateNowPlayingInfo(dummyRadioSong)
	}

	override fun addToQueueSingle(song: DomainSong) {
		_uiState.update { state ->
			val newQueue = state.queue + song
			state.copy(
				queue = newQueue,
				currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
				currentSong = if (state.currentIndex == -1) song else state.currentSong
			)
		}
	}

	override fun addToQueue(collection: DomainSongCollection) {
		_uiState.update { state ->
			val newQueue = state.queue + collection.songs
			state.copy(
				queue = newQueue,
				currentIndex = if (state.currentIndex == -1) 0 else state.currentIndex,
				currentSong = if (state.currentIndex == -1) collection.songs.firstOrNull() else state.currentSong
			)
		}
	}

	override fun removeFromQueue(index: Int) {
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply {
				if (index in indices) removeAt(index)
			}
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

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
		_uiState.update { state ->
			val newQueue = state.queue.toMutableList().apply {
				if (fromIndex in indices && toIndex in 0..size) {
					val item = removeAt(fromIndex)
					add(toIndex, item)
				}
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

	override fun clearQueue() {
		player.replaceCurrentItemWithPlayerItem(null)
		_uiState.update {
			it.copy(queue = emptyList(), currentSong = null, currentIndex = -1, progress = 0f)
		}
		scrobbleManager.onIsPlayingChanged(false)
		updateNowPlayingInfo(null)
	}

	override fun resume() {
		player.play()
		_uiState.update { it.copy(isPaused = false) }
		scrobbleManager.onIsPlayingChanged(true)
		updateNowPlayingInfo(_uiState.value.currentSong)
	}

	override fun pause() {
		player.pause()
		_uiState.update { it.copy(isPaused = true) }
		scrobbleManager.onIsPlayingChanged(false)
		updateNowPlayingInfo(_uiState.value.currentSong)
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

	override fun shufflePlay(collection: DomainSongCollection) {
		val shuffledSongs = collection.songs.shuffled()
		_uiState.update { state ->
			state.copy(
				queue = shuffledSongs,
				currentIndex = 0,
				currentSong = shuffledSongs.firstOrNull()
			)
		}
		playAt(0)
	}

	override fun setPlaybackSpeed(value: Float) {
		player.setRate(value)
		_uiState.update { it.copy(playbackSpeed = value) }
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

	private fun updateNowPlayingInfo(song: DomainSong?) {
		if (song == null) {
			MPNowPlayingInfoCenter.defaultCenter().nowPlayingInfo = null
			return
		}

		val info = mutableMapOf<Any?, Any?>()
		info[MPMediaItemPropertyTitle] = song.title
		info[MPMediaItemPropertyArtist] = song.artistName
		info[MPMediaItemPropertyAlbumTitle] = song.albumTitle
		info[MPNowPlayingInfoPropertyPlaybackRate] = if (_uiState.value.isPaused) 0.0 else 1.0

		val duration = player.currentItem?.duration
		if (duration != null) {
			val seconds = CMTimeGetSeconds(duration)
			if (!seconds.isNaN()) {
				info[MPMediaItemPropertyPlaybackDuration] = seconds
			}
		}

		info[MPNowPlayingInfoPropertyElapsedPlaybackTime] = CMTimeGetSeconds(player.currentTime())

		info[MPMediaItemPropertyArtwork] = MPMediaItemArtwork(
			boundsSize = CGSizeMake(512.0, 512.0),
			requestHandler = {
				return@MPMediaItemArtwork song.coverArtId
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
		if (state.queue.isEmpty() || player.currentItem != null) return

		val index = if (state.currentIndex in 0 until state.queue.size) state.currentIndex else 0
		val song = state.queue.getOrNull(index) ?: return

		val url = getSongUrl(song) ?: return
		player.replaceCurrentItemWithPlayerItem(AVPlayerItem(url))
		player.setRate(state.playbackSpeed)

		if (!song.id.startsWith("radio_")) {
			val durationMs = song.duration.inWholeMilliseconds
			if (durationMs > 0) {
				val positionSeconds = (state.progress * durationMs) / 1000.0
				seekToTime(positionSeconds)
			}
		}

		updateNowPlayingInfo(song)
	}

	private fun getStreamUrl(id: String) =
		when (connectivityManager.isCellular.value) {
			true -> SessionManager.api.getStreamUrl(
				id,
				Settings.shared.streamingQualityCellular.bitrateIos,
				Settings.shared.streamingQualityCellular.containerIos
			)

			false -> SessionManager.api.getStreamUrl(
				id,
				Settings.shared.streamingQualityWifi.bitrateIos,
				Settings.shared.streamingQualityWifi.containerIos
			)
		} + "&estimateContentLength=true"

	private fun getSongUrl(song: DomainSong): NSURL? {
		return when {
			song.id.startsWith("radio_") && !song.filePath.isNullOrEmpty() -> {
				NSURL.URLWithString(song.filePath)
			}
			else -> {
				val localPath = downloadManager.getDownloadedFilePath(song.id)
				if (localPath != null) {
					NSURL.fileURLWithPath(localPath)
				} else {
					NSURL.URLWithString(getStreamUrl(song.id))
				}
			}
		}
	}
}

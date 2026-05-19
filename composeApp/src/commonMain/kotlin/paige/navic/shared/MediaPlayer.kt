package paige.navic.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainRadio
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager

@Serializable
data class PlayerUiState(
	val queue: List<DomainSong> = emptyList(),
	val currentSong: DomainSong? = null,
	val currentCollection: DomainSongCollection? = null,
	val currentIndex: Int = -1,
	val isPaused: Boolean = false,
	val isShuffleEnabled: Boolean = false,
	val repeatMode: Int = 0,
	val progress: Float = 0f,
	val isLoading: Boolean = false,
	val playbackSpeed: Float = 1.0f,
	val playbackBitrate: Int? = null,
	val playbackSampleRate: Int? = null,
	val playbackMimeType: String? = null
)

abstract class MediaPlayerViewModel(
	private val stateRepository: PlayerStateRepository,
	protected val connectivityManager: ConnectivityManager,
	protected val downloadManager: DownloadManager
) : ViewModel() {

	@Suppress("PropertyName")
	protected val _uiState = MutableStateFlow(PlayerUiState())
	val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

	private var statePersistenceJob: Job? = null

	protected fun isAvailable(songId: String): Boolean {
		val isOnline = connectivityManager.isOnline.value
		val isDownloaded = downloadManager.downloadedSongs.value.containsKey(songId)
		return isOnline || isDownloaded
	}

	init {
		viewModelScope.launch {
			SessionManager.activeServerId.collect { serverId ->
				if (serverId != null) {
					statePersistenceJob?.cancel()
					restoreState(serverId)
					observeAndSaveState(serverId)
				}
			}
		}
	}

	abstract fun addToQueueSingle(song: DomainSong)
	abstract fun addToQueue(collection: DomainSongCollection)
	abstract fun removeFromQueue(index: Int)
	abstract fun moveQueueItem(fromIndex: Int, toIndex: Int)
	abstract fun clearQueue()
	abstract fun playAt(index: Int)
	abstract fun playNextSingle(song: DomainSong)
	abstract fun playNext(collection: DomainSongCollection)
	abstract fun playRadio(radio: DomainRadio)
	abstract fun pause()
	abstract fun resume()
	abstract fun seek(normalized: Float)
	abstract fun next()
	abstract fun previous()
	abstract fun toggleShuffle()
	abstract fun toggleRepeat()
	abstract fun shufflePlay(collection: DomainSongCollection)
	abstract fun setPlaybackSpeed(value: Float)

	fun togglePlay() {
		if (!_uiState.value.isPaused) {
			pause()
		} else {
			resume()
		}
	}

	abstract fun syncPlayerWithState(state: PlayerUiState)

	private suspend fun restoreState(serverId: String) {
		try {
			val restoredState = stateRepository.loadState(serverId)

			if (restoredState != null) {
				val stateToApply = restoredState.copy(isPaused = true, isLoading = false)
				_uiState.value = stateToApply
				syncPlayerWithState(stateToApply)
			} else {
				_uiState.value = PlayerUiState()
			}
		} catch (e: Exception) {
			Logger.e("MediaPlayerViewModel", "Failed to restore state for $serverId", e)
			_uiState.value = PlayerUiState()
		}
	}

	@OptIn(FlowPreview::class)
	private fun observeAndSaveState(serverId: String) {
		statePersistenceJob = viewModelScope.launch {
			_uiState
				.debounce(1000L)
				.collect { state ->
					try {
						stateRepository.saveState(serverId, state)
					} catch (e: Exception) {
						Logger.e("MediaPlayerViewModel", "Failed to save state!", e)
					}
				}
		}
	}
}

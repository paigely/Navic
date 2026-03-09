package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.api.model.SongCollection
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import paige.navic.data.session.SessionManager

@Serializable
data class PlayerUiState(
	val queue: List<Song> = emptyList(),
	val currentTrack: Song? = null,
	val currentCollection: SongCollection? = null,
	val currentIndex: Int = -1,
	val isPaused: Boolean = false,
	val isShuffleEnabled: Boolean = false,
	val repeatMode: Int = 0,
	val progress: Float = 0f,
	val isLoading: Boolean = false
)

abstract class MediaPlayerViewModel(
	private val storage: PlayerStateStorage
) : ViewModel() {
	protected val _uiState = MutableStateFlow(PlayerUiState())
	val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

	init {
		viewModelScope.launch {
			restoreState()
			observeAndSaveState()
		}
	}

	abstract  fun addToQueueSingle(track: Song)
	abstract  fun addToQueue(tracks: SongCollection)
	abstract fun removeFromQueue(index: Int)
	abstract fun moveQueueItem(fromIndex: Int, toIndex: Int)
	abstract fun clearQueue()
	abstract fun playAt(index: Int)
	abstract fun pause()
	abstract fun resume()
	abstract fun seek(normalized: Float)
	abstract fun next()
	abstract fun previous()
	abstract fun toggleShuffle()
	abstract fun toggleRepeat()
	abstract fun shufflePlay(tracks: SongCollection)

	fun togglePlay() {
		if (!_uiState.value.isPaused) {
			pause()
		} else {
			resume()
		}
	}

	suspend fun starTrack() {
		_uiState.value.currentTrack?.let {
			SessionManager.api.star(it)
		}
	}

	suspend fun unstarTrack() {
		_uiState.value.currentTrack?.let {
			SessionManager.api.unstar(it)
		}
	}

	abstract fun syncPlayerWithState(state: PlayerUiState)

	private suspend fun restoreState() {
		val savedJson = storage.loadState()
		if (!savedJson.isNullOrBlank()) {
			try {
				val restoredState = Json.decodeFromString<PlayerUiState>(savedJson)
				val stateToApply = restoredState.copy(isPaused = true, isLoading = false)

				_uiState.value = stateToApply

				syncPlayerWithState(stateToApply)

			} catch (e: Exception) {
				println("Failed to restore state: ${e.message}")
				_uiState.value = PlayerUiState()
			}
		}
	}

	@OptIn(FlowPreview::class)
	private fun observeAndSaveState() {
		viewModelScope.launch {
			_uiState
				.debounce(1000L)
				.collect { state ->
					try {
						val jsonString = Json.encodeToString(state)
						storage.saveState(jsonString)
					} catch (e: Exception) {
						println("Failed to save state: ${e.message}")
					}
				}
		}
	}
}

@Composable
expect fun rememberMediaPlayer(): MediaPlayerViewModel
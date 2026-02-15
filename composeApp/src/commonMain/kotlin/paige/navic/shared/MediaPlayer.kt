package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection

data class PlayerUiState(
	val queue: List<Track> = emptyList(),
	val currentTrack: Track? = null,
	val currentCollection: TrackCollection? = null,
	val currentIndex: Int = -1,
	val isPaused: Boolean = false,
	val isShuffleEnabled: Boolean = false,
	val repeatMode: Int = 0,
	val progress: Float = 0f,
	val isLoading: Boolean = false
)

abstract class MediaPlayerViewModel : ViewModel() {
	protected val _uiState = MutableStateFlow(PlayerUiState())
	val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

	abstract  fun addToQueueSingle(track: Track)
	abstract  fun addToQueue(tracks: TrackCollection)
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
	abstract fun shufflePlay(tracks: TrackCollection)

	fun togglePlay() {
		if (!_uiState.value.isPaused) {
			pause()
		} else {
			resume()
		}
	}

	suspend fun starTrack() {
		SessionManager.api.star(_uiState.value.currentTrack?.id?.let { listOf(it) })
	}

	suspend fun unstarTrack() {
		SessionManager.api.unstar(_uiState.value.currentTrack?.id?.let { listOf(it) })
	}
}

@Composable
expect fun rememberMediaPlayer(): MediaPlayerViewModel

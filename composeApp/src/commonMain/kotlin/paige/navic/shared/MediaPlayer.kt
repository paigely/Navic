package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Track
import paige.subsonic.api.model.TrackCollection
import kotlin.time.Clock

data class PlayerUiState(
	val tracks: TrackCollection? = null,
	val currentTrack: Track? = null,
	val currentIndex: Int = -1,
	val isPaused: Boolean = false,
	val progress: Float = 0f,
	val isLoading: Boolean = false
)

abstract class MediaPlayerViewModel : ViewModel() {
	protected val _uiState = MutableStateFlow(PlayerUiState())
	val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

	abstract fun play(tracks: TrackCollection, startIndex: Int)
	abstract fun playSingle(track: Track)
	abstract fun pause()
	abstract fun resume()
	abstract fun seek(normalized: Float)
	abstract fun next()
	abstract fun previous()

	protected fun scrobbleSubmission(oldIndex: Int) {
		viewModelScope.launch {
			val tracks = _uiState.value.tracks?.tracks ?: return@launch
			tracks.getOrNull(oldIndex)?.let { track ->
				try {
					SessionManager.api.scrobble(track.id, Clock.System.now().toEpochMilliseconds(), submission = true)
				} catch (e: Exception) { println(e) }
			}
		}
	}

	protected fun scrobbleNowPlaying(newIndex: Int) {
		viewModelScope.launch {
			val tracks = _uiState.value.tracks?.tracks ?: return@launch
			tracks.getOrNull(newIndex)?.let { track ->
				try {
					SessionManager.api.scrobble(track.id, Clock.System.now().toEpochMilliseconds(), submission = false)
				} catch (e: Exception) { println(e) }
			}
		}
	}
}

@Composable
expect fun rememberMediaPlayer(): MediaPlayerViewModel

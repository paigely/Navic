package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.Settings
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection
import kotlin.time.Clock

data class PlayerUiState(
	val tracks: TrackCollection? = null,
	val currentTrack: Track? = null,
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

	abstract fun play(tracks: TrackCollection, startIndex: Int)
	abstract fun playSingle(track: Track)
	abstract fun pause()
	abstract fun resume()
	abstract fun seek(normalized: Float)
	abstract fun next()
	abstract fun previous()
	abstract fun toggleShuffle()
	abstract fun toggleRepeat()
	abstract fun shufflePlay(tracks: TrackCollection)

	protected fun scrobbleSubmission(trackId: String?) {
		if (!Settings.shared.enableScrobbling) return
		viewModelScope.launch {
			try {
				trackId?.let {
					SessionManager.api.scrobble(
						it,
						Clock.System.now().toEpochMilliseconds(),
						submission = true
					)
				}
			} catch (_: Exception) {}
		}
	}

	protected fun scrobbleNowPlaying(trackId: String?) {
		if (!Settings.shared.enableScrobbling) return
		viewModelScope.launch {
			try {
				trackId?.let {
					SessionManager.api.scrobble(
						it,
						Clock.System.now().toEpochMilliseconds(),
						submission = false
					)
				}
			} catch (_: Exception) {}
		}
	}

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

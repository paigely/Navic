package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.LyricsRepository
import paige.navic.util.UiState
import paige.subsonic.api.model.Track
import kotlin.time.Duration

class LyricsViewModel(
	private val track: Track?,
	private val repository: LyricsRepository = LyricsRepository()
) : ViewModel() {
	private val _lyricsState = MutableStateFlow<UiState<List<Pair<Duration, String>>?>>(UiState.Success(null))
	val lyricsState = _lyricsState.asStateFlow()

	init {
		refreshResults()
	}

	fun refreshResults() {
		viewModelScope.launch {
			if (track == null) {
				_lyricsState.value = UiState.Success(null)
				return@launch
			}
			_lyricsState.value = UiState.Loading
			try {
				_lyricsState.value = UiState.Success(
					repository.fetchLyrics(track)
				)
			} catch (e: Exception) {
				_lyricsState.value = UiState.Error(e)
			}
		}
	}
}
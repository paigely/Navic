package paige.navic.ui.screens.lyrics.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.lyrics.LyricsResult
import paige.navic.domain.repositories.LyricsRepository
import paige.navic.ui.core.UiState

class LyricsScreenViewModel(
	private val song: DomainSong?,
	private val repository: LyricsRepository
) : ViewModel() {
	val lyricsState: StateFlow<UiState<LyricsResult?>>
		field = MutableStateFlow<UiState<LyricsResult?>>(UiState.Loading())

	val listState = LazyListState()

	init {
		refreshResults()
	}

	fun refreshResults() {
		viewModelScope.launch {
			if (song == null) {
				lyricsState.value = UiState.Success(null)
				return@launch
			}
			lyricsState.value = UiState.Loading()
			try {
				lyricsState.value = UiState.Success(
					repository.fetchLyrics(song)
				)
			} catch (e: Exception) {
				lyricsState.value = UiState.Error(e)
			}
		}
	}
}

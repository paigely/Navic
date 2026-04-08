package paige.navic.ui.screens.lyrics.viewmodels

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.LyricRepository
import paige.navic.domain.repositories.LyricsResult
import paige.navic.utils.UiState

class LyricsScreenViewModel(
	private val song: DomainSong?,
	private val repository: LyricRepository
) : ViewModel() {
	private val _lyricsState = MutableStateFlow<UiState<LyricsResult?>>(UiState.Loading())
	val lyricsState = _lyricsState.asStateFlow()

	val listState = LazyListState()

	init {
		refreshResults()
	}

	fun refreshResults() {
		viewModelScope.launch {
			if (song == null) {
				_lyricsState.value = UiState.Success(null)
				return@launch
			}
			_lyricsState.value = UiState.Loading()
			try {
				_lyricsState.value = UiState.Success(
					repository.fetchLyrics(song)
				)
			} catch (e: Exception) {
				_lyricsState.value = UiState.Error(e)
			}
		}
	}
}

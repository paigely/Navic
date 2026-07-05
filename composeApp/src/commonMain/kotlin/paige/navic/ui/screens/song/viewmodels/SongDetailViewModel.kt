package paige.navic.ui.screens.song.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.CollectionRepository
import paige.navic.ui.core.UiState

class SongDetailViewModel(
	songId: String,
	private val repository: CollectionRepository
) : ViewModel() {
	val songState: StateFlow<UiState<DomainSong>>
		field = MutableStateFlow<UiState<DomainSong>>(UiState.Loading())

	init {
		viewModelScope.launch {
			val song = repository.getSongById(songId)
			if (song != null) {
				songState.value = UiState.Success(song)
			} else {
				songState.value = UiState.Error(Exception("Unknown song"))
			}
		}
	}
}

package paige.navic.ui.screens.track.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.models.DomainSong
import paige.navic.domain.repositories.TrackRepository
import paige.navic.utils.UiState

class TrackDetailViewModel(
	songId: String,
	private val repository: TrackRepository
) : ViewModel() {
	private val _songState = MutableStateFlow<UiState<DomainSong>>(UiState.Loading())
	val songState = _songState.asStateFlow()

	init {
		viewModelScope.launch {
			val song = repository.getSongById(songId)
			if (song != null) {
				_songState.value = UiState.Success(song)
			} else {
				_songState.value = UiState.Error(Exception("Unknown song"))
			}
		}
	}
}

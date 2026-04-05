package paige.navic.ui.screens.genre.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.repositories.GenreRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainGenre
import paige.navic.utils.UiState

class GenreListViewModel(
	private val repository: GenreRepository
) : ViewModel() {
	private val _genresState = MutableStateFlow<UiState<List<DomainGenre>>>(UiState.Loading())
	val genresState = _genresState.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshGenres(false) }
		}
	}

	fun refreshGenres(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getGenresFlow(fullRefresh).collect {
				_genresState.value = it
			}
		}
	}

	fun clearError() {
		_genresState.value = UiState.Success(_genresState.value.data.orEmpty())
	}
}
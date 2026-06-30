package paige.navic.ui.screens.genre.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainGenre
import paige.navic.domain.repositories.GenreRepository
import paige.navic.ui.core.UiState

class GenreListViewModel(
	private val repository: GenreRepository,
	private val sessionManager: SessionManager
) : ViewModel() {
	val genresState: StateFlow<UiState<ImmutableList<DomainGenre>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainGenre>>>(UiState.Loading())

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshGenres(false) }
		}
	}

	fun refreshGenres(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getGenresFlow(fullRefresh).collect {
				genresState.value = it
			}
		}
	}

	fun clearError() {
		genresState.value = UiState.Success(genresState.value.data ?: persistentListOf())
	}
}

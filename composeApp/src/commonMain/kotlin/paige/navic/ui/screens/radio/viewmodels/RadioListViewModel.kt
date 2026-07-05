package paige.navic.ui.screens.radio.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainRadio
import paige.navic.domain.repositories.RadioRepository
import paige.navic.ui.core.UiState

class RadioListViewModel(
	private val repository: RadioRepository,
	private val sessionManager: SessionManager
) : ViewModel() {
	val radiosState: StateFlow<UiState<ImmutableList<DomainRadio>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainRadio>>>(UiState.Loading())

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshRadios(false) }
		}
	}

	fun refreshRadios(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getRadiosFlow(fullRefresh).collect {
				radiosState.value = it
			}
		}
	}

	fun clearError() {
		radiosState.value = UiState.Success(radiosState.value.data ?: persistentListOf())
	}
}

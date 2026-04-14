package paige.navic.ui.screens.radio.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainRadio
import paige.navic.domain.repositories.RadioRepository
import paige.navic.utils.UiState

class RadioListViewModel(
	private val repository: RadioRepository
) : ViewModel() {
	private val _radiosState =
		MutableStateFlow<UiState<ImmutableList<DomainRadio>>>(UiState.Loading())
	val radiosState = _radiosState.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshRadios(false) }
		}
	}

	fun refreshRadios(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getRadiosFlow(fullRefresh).collect {
				_radiosState.value = it
			}
		}
	}

	fun clearError() {
		_radiosState.value = UiState.Success(_radiosState.value.data ?: persistentListOf())
	}
}

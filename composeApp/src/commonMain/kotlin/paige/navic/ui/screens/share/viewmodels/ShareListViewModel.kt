package paige.navic.ui.screens.share.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainShare
import paige.navic.domain.repositories.ShareRepository
import paige.navic.ui.core.UiState

class ShareListViewModel(
	private val repository: ShareRepository,
	private val sessionManager: SessionManager
) : ViewModel() {
	val sharesState: StateFlow<UiState<List<DomainShare>>>
		field = MutableStateFlow<UiState<List<DomainShare>>>(UiState.Loading())

	val isRefreshing: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect {
				refreshShares()
			}
		}
	}

	fun refreshShares() {
		viewModelScope.launch {
			val currentState = sharesState.value
			val hasData = currentState is UiState.Success && currentState.data.isNotEmpty()

			if (hasData) {
				isRefreshing.value = true
			} else {
				sharesState.value = UiState.Loading()
			}

			try {
				val shares = repository.getShares()
				sharesState.value = UiState.Success(shares)
			} catch (e: Exception) {
				sharesState.value = UiState.Error(e)
			} finally {
				isRefreshing.value = false
			}
		}
	}
}

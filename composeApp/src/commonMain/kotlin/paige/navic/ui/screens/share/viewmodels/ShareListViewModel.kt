package paige.navic.ui.screens.share.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.repositories.ShareRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainShare
import paige.navic.utils.UiState

class ShareListViewModel(
	private val repository: ShareRepository = ShareRepository()
) : ViewModel() {
	private val _sharesState = MutableStateFlow<UiState<List<DomainShare>>>(UiState.Loading())
	val sharesState = _sharesState.asStateFlow()

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing = _isRefreshing.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshShares()
			}
		}
	}

	fun refreshShares() {
		viewModelScope.launch {
			val currentState = _sharesState.value
			val hasData = currentState is UiState.Success && currentState.data.isNotEmpty()

			if (hasData) {
				_isRefreshing.value = true
			} else {
				_sharesState.value = UiState.Loading()
			}

			try {
				val shares = repository.getShares()
				_sharesState.value = UiState.Success(shares)
			} catch (e: Exception) {
				_sharesState.value = UiState.Error(e)
			} finally {
				_isRefreshing.value = false
			}
		}
	}
}
package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.zt64.subsonic.api.model.Share
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repositories.SharesRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState

class SharesViewModel(
	private val repository: SharesRepository = SharesRepository()
) : ViewModel() {
	private val _sharesState = MutableStateFlow<UiState<List<Share>>>(UiState.Loading)
	val sharesState = _sharesState.asStateFlow()

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing = _isRefreshing.asStateFlow()

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
				_sharesState.value = UiState.Loading
			}

			try {
				val shares = repository.getShares()
				_sharesState.value = UiState.Success(shares)
			} catch (e: Exception) {
				if (!hasData) {
					_sharesState.value = UiState.Error(e)
				}
			} finally {
				_isRefreshing.value = false
			}
		}
	}
}
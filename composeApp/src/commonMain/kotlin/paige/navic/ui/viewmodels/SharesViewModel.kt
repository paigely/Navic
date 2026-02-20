package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repositories.SharesRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import paige.subsonic.api.models.Share

class SharesViewModel(
	private val repository: SharesRepository = SharesRepository()
) : ViewModel() {
	private val _sharesState = MutableStateFlow<UiState<List<Share>>>(UiState.Loading)
	val sharesState = _sharesState.asStateFlow()

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing = _isRefreshing.asStateFlow()

	private val _selectedShare = MutableStateFlow<Share?>(null)
	val selectedShare: StateFlow<Share?> = _selectedShare.asStateFlow()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshShares()
			}
		}
	}

	fun selectShare(share: Share) {
		_selectedShare.value = share
	}

	fun clearSelection() {
		_selectedShare.value = null
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
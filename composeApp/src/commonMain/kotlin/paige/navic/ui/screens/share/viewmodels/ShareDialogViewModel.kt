package paige.navic.ui.screens.share.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import kotlin.time.Clock
import kotlin.time.Duration

class ShareDialogViewModel : ViewModel() {
	private val _state = MutableStateFlow<UiState<String?>>(UiState.Success(null))
	val state = _state.asStateFlow()

	fun share(
		id: String,
		expiry: Duration?
	) {
		viewModelScope.launch {
			_state.value = UiState.Loading()
			try {
				val expiration = expiry?.let { Clock.System.now() + it  }
				val url = SessionManager.api
					.createShare(listOf(id), expiresAt = expiration)
					.url
				_state.value = UiState.Success(url)
			} catch(e: Exception) {
				_state.value = UiState.Error(e)
			}
		}
	}
}

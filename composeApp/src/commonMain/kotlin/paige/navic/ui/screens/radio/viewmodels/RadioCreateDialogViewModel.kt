package paige.navic.ui.screens.radio.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.notice_created_radio
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.SnackBarManager
import paige.navic.ui.core.UiState

class RadioCreateDialogViewModel(
	private val sessionManager: SessionManager,
	private val snackBarManager: SnackBarManager
) : ViewModel() {
	val creationState: StateFlow<UiState<Nothing?>>
		field = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	val name = TextFieldState()
	val streamUrl = TextFieldState()
	val homepageUrl = TextFieldState()

	fun create() {
		viewModelScope.launch {
			creationState.value = UiState.Loading()
			try {
				sessionManager.api.createInternetRadioStation(
					name = name.text.toString(),
					streamUrl = streamUrl.text.toString(),
					homepageUrl = homepageUrl.text.toString().trim().takeIf { it.isNotBlank() }
				)
				_events.send(Event.Dismiss)
				creationState.value = UiState.Success(null)
				snackBarManager.notify(Res.string.notice_created_radio, name.text.toString())
			} catch (e: Exception) {
				creationState.value = UiState.Error(e)
			}
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}

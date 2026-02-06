package paige.navic.ui.viewmodel

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.model.User
import paige.navic.data.session.SessionManager
import paige.navic.util.LoginState

class LoginViewModel : ViewModel() {
	private val _loginState = MutableStateFlow<LoginState<User?>>(LoginState.LoggedOut)
	val loginState: StateFlow<LoginState<User?>> = _loginState.asStateFlow()

	var instanceError by mutableStateOf<String?>(null)
		private set

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	init {
		loadUser()
		viewModelScope.launch {
			snapshotFlow { instanceState.text }
				.collect {
					if (instanceError != null) {
						instanceError = null
					}
				}
		}
	}

	fun loadUser() {
		viewModelScope.launch {
			val user = SessionManager.currentUser
			if (user != null) {
				_loginState.value = LoginState.Success(user)
			} else {
				_loginState.value = LoginState.LoggedOut
			}
		}
	}

	fun login() {
		val rawInput = instanceState.text.toString()

		if (rawInput.isBlank()) {
			instanceError = "Instance can't be empty"
			return
		}

		if (!Regex("^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})(:\\d{1,5})?([/\\w .-]*)*/?\$").matches(rawInput)) {
			instanceError = "Invalid instance format"
			return
		}

		viewModelScope.launch {
			_loginState.value = LoginState.Loading
			_loginState.value = try {
				SessionManager.login(
					rawInput.let {
						if (!it.startsWith("https://") && !it.startsWith("http://"))
							"https://$it"
						else it
					},
					usernameState.text.toString(),
					passwordState.text.toString()
				)
				if (SessionManager.currentUser != null) {
					LoginState.Success(SessionManager.currentUser)
				} else {
					throw Exception("currentUser is null")
				}
			} catch (e: Exception) {
				LoginState.Error(e)
			}
		}
	}

	fun logout() {
		viewModelScope.launch {
			SessionManager.logout()
			_loginState.value = LoginState.LoggedOut
		}
	}
}
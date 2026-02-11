package paige.navic.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.User
import paige.navic.data.session.SessionManager
import paige.navic.utils.LoginState

class LoginViewModel : ViewModel() {
	private val _loginState = MutableStateFlow<LoginState<User?>>(LoginState.LoggedOut)
	val loginState: StateFlow<LoginState<User?>> = _loginState.asStateFlow()

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	init {
		loadUser()
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
		viewModelScope.launch {
			_loginState.value = LoginState.Loading
			_loginState.value = try {
				SessionManager.login(
					instanceState.text.toString().let {
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
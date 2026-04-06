package paige.navic.ui.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.User
import paige.navic.domain.repositories.DbRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.LoginState
import kotlin.onFailure

class LoginViewModel(
	private val repository: DbRepository
) : ViewModel() {
	private val _loginState = MutableStateFlow<LoginState<User?>>(LoginState.Idle)
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
				_loginState.value = LoginState.Idle
			}
		}
	}

	fun login() {
		viewModelScope.launch {
			_loginState.value = LoginState.Loading

			try {
				val url = instanceState.text.toString().let {
					if (!it.startsWith("https://") && !it.startsWith("http://")) "https://$it" else it
				}

				SessionManager.login(
					url,
					usernameState.text.toString(),
					passwordState.text.toString()
				)

				val user = SessionManager.currentUser ?: throw Exception("currentUser is null")

				repository.syncEverything { progress, message ->
					_loginState.value = LoginState.Syncing(progress, message)
				}.onSuccess {
					_loginState.value = LoginState.Success(user)
				}.onFailure { e ->
					_loginState.value = LoginState.Error(e as Exception)
				}

			} catch (e: Exception) {
				_loginState.value = LoginState.Error(e)
			}
		}
	}

	fun logout() {
		viewModelScope.launch {
			repository.removeEverything()

			SessionManager.logout()
			_loginState.value = LoginState.Idle
		}
	}
}
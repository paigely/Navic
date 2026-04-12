package paige.navic.ui.screens.login.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.User
import paige.navic.data.session.SessionManager
import paige.navic.domain.repositories.DbRepository
import paige.navic.utils.LoginState

class LoginViewModel(
	private val repository: DbRepository
) : ViewModel() {
	private val _loginState = MutableStateFlow<LoginState<User?>>(LoginState.Idle)
	val loginState = _loginState.asStateFlow()

	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	var instanceError by mutableStateOf(false)
		private set
	var usernameError by mutableStateOf(false)
		private set
	var passwordError by mutableStateOf(false)
		private set

	fun validateInstance() {
		instanceError = instanceState.text.isBlank()
	}

	fun validateUsername() {
		usernameError = usernameState.text.isBlank()
	}

	fun validatePassword() {
		passwordError = passwordState.text.isBlank()
	}

	fun validateStuff(): Boolean {
		validateInstance()
		validateUsername()
		validatePassword()
		return !instanceError && !usernameError && !passwordError
	}

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

	fun login(): Boolean {
		if (!validateStuff()) return false

		viewModelScope.launch {
			_loginState.value = LoginState.Loading

			try {
				val url = instanceState.text.toString().let {
					if (!it.startsWith("https://") && !it.startsWith("http://")) "https://$it" else it
				}.trim()

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

		return true
	}

	fun logout() {
		_loginState.value = LoginState.Idle
		SessionManager.logout()
		viewModelScope.launch {
			repository.removeEverything()
		}
	}
}

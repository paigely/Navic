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
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.repositories.DbRepository
import paige.navic.ui.core.LoginUiState

class LoginViewModel(
	private val repository: DbRepository,
	private val sessionManager: SessionManager
) : ViewModel() {
	private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
	val loginState = _loginUiState.asStateFlow()

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
			if (sessionManager.isLoggedIn.value) {
				_loginUiState.value = LoginUiState.Success
			} else {
				_loginUiState.value = LoginUiState.Idle
			}
		}
	}

	fun login(): Boolean {
		if (!validateStuff()) return false

		viewModelScope.launch {
			_loginUiState.value = LoginUiState.Loading

			try {
				val url = instanceState.text.toString().let {
					if (!it.startsWith("https://") && !it.startsWith("http://")) "https://$it" else it
				}.trim()

				sessionManager.login(
					url,
					usernameState.text.toString(),
					passwordState.text.toString()
				)

				repository.syncEverything { progress, message ->
					_loginUiState.value = LoginUiState.Syncing(progress, message)
				}.onSuccess {
					_loginUiState.value = LoginUiState.Success
				}.onFailure { e ->
					_loginUiState.value = LoginUiState.Error(e as Exception)
				}

			} catch (e: Exception) {
				_loginUiState.value = LoginUiState.Error(e)
			}
		}

		return true
	}

	fun logout() {
		_loginUiState.value = LoginUiState.Idle
		sessionManager.logout()
		viewModelScope.launch {
			repository.removeEverything()
		}
	}
}

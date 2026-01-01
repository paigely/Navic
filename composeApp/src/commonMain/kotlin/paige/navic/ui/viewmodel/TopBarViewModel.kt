package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.model.User
import paige.navic.data.repository.TopBarRepository
import paige.navic.util.LoginState

class TopBarViewModel(
	private val repository: TopBarRepository = TopBarRepository()
) : ViewModel() {
	private val _userState = MutableStateFlow<LoginState<User?>>(LoginState.LoggedOut)
	val userState: StateFlow<LoginState<User?>> = _userState.asStateFlow()

	init {
		loadUser()
	}

	fun loadUser() {
		viewModelScope.launch {
			val user = repository.getLoggedInUser()
			if (user != null) {
				_userState.value = LoginState.Success(user)
			} else {
				_userState.value = LoginState.LoggedOut
			}
		}
	}

	fun login(
		instanceUrl: String,
		username: String,
		password: String
	) {
		viewModelScope.launch {
			_userState.value = LoginState.Loading
			_userState.value = try {
				val user = repository.login(instanceUrl, username, password)
				LoginState.Success(user)
			} catch (e: Exception) {
				LoginState.Error(e)
			}
		}
	}

	fun logout() {
		viewModelScope.launch {
			repository.logout()
			_userState.value = LoginState.LoggedOut
		}
	}
}

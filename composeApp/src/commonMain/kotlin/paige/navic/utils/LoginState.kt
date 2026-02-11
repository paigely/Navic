package paige.navic.utils

sealed class LoginState<out T> {
	object Loading : LoginState<Nothing>()
	object LoggedOut : LoginState<Nothing>()
	data class Success<T>(val data: T) : LoginState<T>()
	data class Error(val error: Exception) : LoginState<Nothing>()
}

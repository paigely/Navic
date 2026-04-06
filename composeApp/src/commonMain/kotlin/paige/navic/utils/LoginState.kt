package paige.navic.utils

sealed class LoginState<out T> {
	object Idle : LoginState<Nothing>()
	object Loading : LoginState<Nothing>()
	data class Syncing(val progress: Float, val message: String? = null) : LoginState<Nothing>()
	data class Success<T>(val data: T) : LoginState<T>()
	data class Error(val error: Exception) : LoginState<Nothing>()
}

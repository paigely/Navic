package paige.navic.utils

import org.jetbrains.compose.resources.StringResource

sealed class LoginState<out T> {
	object Idle : LoginState<Nothing>()
	object Loading : LoginState<Nothing>()
	data class Syncing(val progress: Float, val message: StringResource) : LoginState<Nothing>()
	data class Success<T>(val data: T) : LoginState<T>()
	data class Error(val error: Exception) : LoginState<Nothing>()
}

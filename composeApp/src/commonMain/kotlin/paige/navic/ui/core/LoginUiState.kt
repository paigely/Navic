package paige.navic.ui.core

import org.jetbrains.compose.resources.StringResource

sealed class LoginUiState<out T> {
	object Idle : LoginUiState<Nothing>()
	object Loading : LoginUiState<Nothing>()
	data class Syncing(val progress: Float, val message: StringResource) : LoginUiState<Nothing>()
	data class Success<T>(val data: T) : LoginUiState<T>()
	data class Error(val error: Exception) : LoginUiState<Nothing>()
}

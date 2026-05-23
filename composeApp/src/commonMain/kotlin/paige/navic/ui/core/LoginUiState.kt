package paige.navic.ui.core

import org.jetbrains.compose.resources.StringResource

sealed class LoginUiState {
	object Idle : LoginUiState()
	object Loading : LoginUiState()
	object Success : LoginUiState()
	data class Syncing(val progress: Float, val message: StringResource) : LoginUiState()
	data class Error(val error: Exception) : LoginUiState()
}

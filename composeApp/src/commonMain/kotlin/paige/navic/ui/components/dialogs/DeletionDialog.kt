package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.info_action_is_permanent
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.notice_deleted_playlist
import navic.composeapp.generated.resources.notice_deleted_share
import navic.composeapp.generated.resources.title_delete_playlist
import navic.composeapp.generated.resources.title_delete_share
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalSnackbarState
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState

enum class DeletionEndpoint(
	val questionText: StringResource,
	val deletedText: StringResource
) {
	PLAYLIST(Res.string.title_delete_playlist, Res.string.notice_deleted_playlist),
	SHARE(Res.string.title_delete_share, Res.string.notice_deleted_share)
}

class DeletionViewModel : ViewModel() {
	private val _state = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))
	val state = _state.asStateFlow()

	fun delete(
		endpoint: DeletionEndpoint,
		id: String
	) {
		viewModelScope.launch {
			_state.value = UiState.Loading
			try {
				when (endpoint) {
					DeletionEndpoint.PLAYLIST -> SessionManager.api.deletePlaylist(id)
					DeletionEndpoint.SHARE -> SessionManager.api.deleteShare(id)
				}
				_state.value = UiState.Success(null)
			} catch(e: Exception) {
				_state.value = UiState.Error(e)
			}
		}
	}
}

@Composable
fun DeletionDialog(
	viewModel: DeletionViewModel = viewModel { DeletionViewModel() },
	endpoint: DeletionEndpoint,
	id: String?,
	onIdClear: () -> Unit
) {

	val snackbarState = LocalSnackbarState.current
	val scrollState = rememberScrollState()
	val state by viewModel.state.collectAsState()

	LaunchedEffect(state) {
		if (state is UiState.Success && id != null) {
			viewModel.viewModelScope.launch {
				onIdClear()
				snackbarState.showSnackbar(
					getString(endpoint.deletedText)
				)
			}
		}
	}

	id?.let {
		AlertDialog(
			title = { Text(stringResource(endpoint.questionText)) },
			text = {
				Column(Modifier.verticalScroll(scrollState)) {
					Text(
						stringResource(
							if (state !is UiState.Error)
								Res.string.info_action_is_permanent
							else Res.string.info_error
						)
					)
					(state as? UiState.Error)?.error?.let {
						SelectionContainer {
							Text("$it")
						}
					}
				}
			},
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onIdClear()
				}
			},
			confirmButton = {
				Button(
					onClick = { viewModel.delete(endpoint, id) },
					enabled = state !is UiState.Loading,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.error
					),
					shape = ContinuousCapsule
				) {
					if (state !is UiState.Loading) {
						Text(stringResource(Res.string.action_delete))
					} else {
						CircularProgressIndicator(Modifier.size(20.dp))
					}
				}
			},
			dismissButton = {
				TextButton(
					enabled = state !is UiState.Loading,
					onClick = onIdClear,
				) { Text(stringResource(Res.string.action_cancel)) }
			},
			shape = ContinuousRoundedRectangle(42.dp)
		)
	}
}

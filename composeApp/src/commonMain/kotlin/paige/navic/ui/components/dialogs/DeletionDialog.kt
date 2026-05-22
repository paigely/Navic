package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalSnackbarState
import paige.navic.data.database.SyncManager
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.entities.SyncActionType
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.ui.components.common.FormButton
import paige.navic.utils.UiState

enum class DeletionEndpoint(
	val questionText: StringResource,
	val deletedText: StringResource
) {
	PLAYLIST(Res.string.title_delete_playlist, Res.string.notice_deleted_playlist),
	SHARE(Res.string.title_delete_share, Res.string.notice_deleted_share)
}

class DeletionViewModel(
	private val syncManager: SyncManager,
	private val playlistDao: PlaylistDao
) : ViewModel() {
	private val _state = MutableStateFlow<UiState<Nothing?>>(UiState.Success(null))
	val state = _state.asStateFlow()

	private val _events = Channel<Event>()
	val events = _events.receiveAsFlow()

	fun delete(
		endpoint: DeletionEndpoint,
		id: String
	) {
		viewModelScope.launch {
			_state.value = UiState.Loading()
			try {
				if (endpoint == DeletionEndpoint.SHARE) {
					SessionManager.api.deleteShare(id)
				} else {
					syncManager.enqueueAction(
						actionType = SyncActionType.DELETE_PLAYLIST,
						itemId = id
					)
					playlistDao.deletePlaylist(id)
				}
				_state.value = UiState.Success(null)
				_events.send(Event.Dismiss)
			} catch (error: Exception) {
				_state.value = UiState.Error(error = error)
			}
		}
	}

	sealed class Event {
		object Dismiss : Event()
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DeletionDialog(
	endpoint: DeletionEndpoint,
	id: String?,
	onIdClear: () -> Unit,
	onRefresh: () -> Unit
) {
	val viewModel = koinViewModel<DeletionViewModel>()
	val snackbarState = LocalSnackbarState.current
	val state by viewModel.state.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is DeletionViewModel.Event.Dismiss -> {
					onIdClear()
					onRefresh()
					snackbarState.showSnackbar(getString(endpoint.deletedText))
				}
			}
		}
	}

	id?.let {
		FormDialog(
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onIdClear()
				}
			},
			icon = { Icon(Icons.Outlined.Delete, null) },
			title = { Text(stringResource(endpoint.questionText)) },
			buttons = {
				FormButton(
					onClick = { viewModel.delete(endpoint, id) },
					color = MaterialTheme.colorScheme.error
				) {
					if (state is UiState.Loading) {
						CircularProgressIndicator(Modifier.size(20.dp))
					}
					Text(stringResource(Res.string.action_delete))
				}
				FormButton(onClick = onIdClear) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
			content = {
				(state as? UiState.Error)?.error?.let {
					SelectionContainer {
						Text("$it")
					}
				}
				Text(
					stringResource(
						if (state !is UiState.Error)
							Res.string.info_action_is_permanent
						else Res.string.info_error
					)
				)
			}
		)
	}
}

package paige.navic.ui.screens.playlist.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_playlist_name
import navic.composeapp.generated.resources.title_create_playlist
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.ui.components.common.FormButton
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.screens.playlist.viewmodels.PlaylistCreateDialogViewModel
import paige.navic.utils.UiState

@Composable
fun PlaylistCreateDialog(
	onDismissRequest: () -> Unit,
	onRefresh: () -> Unit,
	tracks: ImmutableList<DomainSong> = persistentListOf(),
	navigateAfterwards: Boolean = true
) {
	val viewModel = koinViewModel<PlaylistCreateDialogViewModel>(
		key = tracks.joinToString { it.id },
		parameters = { parametersOf(tracks) }
	)
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val state by viewModel.creationState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is PlaylistCreateDialogViewModel.Event.Dismiss -> {
					onDismissRequest()
					onRefresh()
					if (navigateAfterwards) {
						if (backStack.contains(Screen.NowPlaying)) {
							backStack.remove(Screen.NowPlaying)
						}
						backStack.add(Screen.TrackList(event.playlist.id, "playlists"))
					}
				}
			}
		}
	}

	FormDialog(
		onDismissRequest = onDismissRequest,
		icon = { Icon(Icons.Outlined.PlaylistAdd, null) },
		title = { Text(stringResource(Res.string.title_create_playlist)) },
		buttons = {
			FormButton(
				onClick = {
					ctx.clickSound()
					viewModel.create()
				},
				enabled = state !is UiState.Loading && viewModel.name.text.isNotBlank(),
				color = MaterialTheme.colorScheme.primary
			) {
				if (state !is UiState.Loading) {
					Text(stringResource(Res.string.action_ok))
				} else {
					CircularProgressIndicator(
						modifier = Modifier.size(20.dp)
					)
				}
			}
			FormButton(
				onClick = {
					ctx.clickSound()
					onDismissRequest()
				},
				enabled = state !is UiState.Loading,
				content = { Text(stringResource(Res.string.action_cancel)) }
			)
		},
		content = {
			(state as? UiState.Error)?.error?.let {
				SelectionContainer {
					Text("$it")
				}
			}
			TextField(
				state = viewModel.name,
				label = { Text(stringResource(Res.string.option_playlist_name)) },
				lineLimits = TextFieldLineLimits.SingleLine
			)
		}
	)
}
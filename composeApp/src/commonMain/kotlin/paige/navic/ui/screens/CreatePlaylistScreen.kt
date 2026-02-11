package paige.navic.ui.screens

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_playlist_name
import navic.composeapp.generated.resources.title_create_playlist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Badge
import paige.navic.ui.components.layouts.CustomDialog
import paige.navic.ui.viewmodels.CreatePlaylistViewModel
import paige.navic.utils.UiState
import paige.subsonic.api.models.Track

@Composable
fun CreatePlaylistScreen(
	tracks: List<Track>,
	viewModel: CreatePlaylistViewModel = viewModel(key = tracks.joinToString()) { CreatePlaylistViewModel(tracks) }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val state by viewModel.creationState.collectAsState()

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				is CreatePlaylistViewModel.Event.Dismiss -> {
					if (backStack.lastOrNull() is Screen.CreatePlaylist) {
						backStack.removeLastOrNull()
						backStack.add(Screen.Tracks(event.playlist))
					}
				}
			}
		}
	}

	CustomDialog(
		title = { Text(stringResource(Res.string.title_create_playlist)) },
		buttons = {
			TextButton(
				onClick = {
					ctx.clickSound()
					if (backStack.lastOrNull() is Screen.CreatePlaylist) {
						backStack.removeLastOrNull()
					}
				},
				enabled = state !is UiState.Loading,
				content = { Text(stringResource(Res.string.action_cancel)) }
			)
			Button(
				onClick = {
					ctx.clickSound()
					viewModel.create()
				},
				enabled = state !is UiState.Loading,
			) {
				if (state !is UiState.Loading) {
					Text(stringResource(Res.string.action_ok))
				} else {
					CircularProgressIndicator(
						modifier = Modifier.size(20.dp)
					)
				}
			}
		}
	) {
		OutlinedTextField(
			state = viewModel.name,
			leadingIcon = { Icon(Icons.Outlined.Badge, null) },
			label = { Text(stringResource(Res.string.option_playlist_name)) }
		)
		// TODO: list of songs and adding songs
	}
}
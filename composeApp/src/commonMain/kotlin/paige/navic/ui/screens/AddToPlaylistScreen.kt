package paige.navic.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.action_refresh
import navic.composeapp.generated.resources.info_no_other_playlists
import navic.composeapp.generated.resources.info_no_playlists
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.Refresh
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.layouts.CustomDialog
import paige.navic.ui.viewmodels.AddToPlaylistViewModel
import paige.navic.utils.UiState
import paige.subsonic.api.models.Playlist
import paige.subsonic.api.models.Track

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddToPlaylistScreen(
	tracks: List<Track>,
	playlistToExclude: String?,
	viewModel: AddToPlaylistViewModel = viewModel(
		key = tracks.joinToString() + playlistToExclude
	) { AddToPlaylistViewModel(tracks, playlistToExclude) }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val state by viewModel.playlistsState.collectAsState()
	val confirmState by viewModel.confirmState.collectAsState()
	val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

	val list: @Composable (playlists: List<Playlist>) -> Unit = { playlists ->
		LazyColumn(Modifier.selectableGroup().clip(MaterialTheme.shapes.largeIncreased)) {
			items(playlists) { playlist ->
				ListItem(
					modifier = Modifier
						.selectable(
							selected = (playlist == selectedPlaylist),
							onClick = {
								ctx.clickSound()
								viewModel.selectPlaylist(playlist)
							},
							role = Role.RadioButton
						),
					headlineContent = {
						Text(playlist.title)
					},
					supportingContent = {
						playlist.subtitle?.let { subtitle ->
							Text(subtitle)
						}
					},
					leadingContent = {
						RadioButton(
							selected = (playlist == selectedPlaylist),
							onClick = null
						)
					}
				)
			}
		}
	}
	val buttons: @Composable (playlists: List<Playlist>) -> Unit = { playlists ->
		TextButton(
			onClick = {
				ctx.clickSound()
				if (backStack.lastOrNull() is Screen.AddToPlaylist) {
					backStack.removeLastOrNull()
				}
			},
			content = { Text(stringResource(Res.string.action_cancel)) }
		)
		val colors = ButtonDefaults.filledTonalButtonColors()
		SplitButtonLayout(
			leadingButton = {
				if (playlists.isNotEmpty()) {
					SplitButtonDefaults.LeadingButton(
						onClick = {
							ctx.clickSound()
							viewModel.confirm()
						},
						enabled = confirmState !is UiState.Loading && selectedPlaylist != null,
						colors = colors
					) {
						if (confirmState !is UiState.Loading) {
							Text(stringResource(Res.string.action_ok))
						} else {
							CircularProgressIndicator(
								modifier = Modifier.size(20.dp)
							)
						}
					}
				} else {
					SplitButtonDefaults.LeadingButton(
						onClick = {
							ctx.clickSound()
							if (backStack.lastOrNull() is Screen.AddToPlaylist) {
								backStack.removeLastOrNull()
								backStack.add(Screen.CreatePlaylist(tracks))
							}
						},
						colors = colors
					) {
						Icon(Icons.Outlined.PlaylistAdd, null)
						Spacer(Modifier.width(8.dp))
						Text(stringResource(Res.string.action_new))
					}
				}
			},
			trailingButton = {
				SplitButtonDefaults.TrailingButton(
					onClick = {
						ctx.clickSound()
						viewModel.refreshResults()
					},
					colors = colors
				) {
					Icon(
						imageVector = Icons.Outlined.Refresh,
						contentDescription = stringResource(Res.string.action_refresh)
					)
				}
			}
		)
	}

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				AddToPlaylistViewModel.Event.Dismiss -> {
					if (backStack.lastOrNull() is Screen.AddToPlaylist) {
						backStack.removeLastOrNull()
					}
				}
			}
		}
	}

	when (val state = state) {
		is UiState.Loading -> CustomDialog {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				ContainedLoadingIndicator(Modifier.size(48.dp))
			}
		}
		is UiState.Error -> CustomDialog {
			Box(
				modifier = Modifier.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				ErrorBox(state)
			}
		}
		is UiState.Success -> {
			val playlists = state.data
			CustomDialog(
				subtitle = {
					if (playlists.isEmpty()) {
						Text(stringResource(
							if (playlistToExclude != null)
								Res.string.info_no_other_playlists
							else Res.string.info_no_playlists
						))
					}
				},
				buttons = { buttons(playlists) },
				content = { list(playlists) }
			)
		}
	}
}

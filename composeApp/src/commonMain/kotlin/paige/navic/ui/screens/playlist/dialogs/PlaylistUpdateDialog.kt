package paige.navic.ui.screens.playlist.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import dev.zt64.subsonic.api.model.Playlist
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.action_refresh
import navic.composeapp.generated.resources.info_no_other_playlists
import navic.composeapp.generated.resources.info_no_playlists
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalCtx
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.Refresh
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.FormButton
import paige.navic.ui.components.dialogs.FormDialog
import paige.navic.ui.screens.playlist.viewmodels.PlaylistUpdateDialogViewModel
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistUpdateDialog(
	tracks: ImmutableList<DomainSong>,
	playlistToExclude: String? = null,
	onDismissRequest: () -> Unit
) {
	val viewModel = koinViewModel<PlaylistUpdateDialogViewModel>(
		key = tracks.joinToString() + playlistToExclude,
		parameters = { parametersOf(tracks, playlistToExclude) }
	)
	val ctx = LocalCtx.current
	val state by viewModel.playlistsState.collectAsState()
	val confirmState by viewModel.confirmState.collectAsState()
	val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

	var createDialogShown by rememberSaveable { mutableStateOf(false) }

	val list: @Composable (playlists: List<Playlist>) -> Unit = { playlists ->
		LazyColumn(
			modifier = Modifier
				.selectableGroup()
				.clip(MaterialTheme.shapes.largeIncreased)
				.heightIn(max = 300.dp)
		) {
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
						Text(playlist.name)
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

	LaunchedEffect(Unit) {
		viewModel.events.collect { event ->
			when (event) {
				PlaylistUpdateDialogViewModel.Event.Dismiss -> {
					onDismissRequest()
				}
			}
		}
	}

	FormDialog(
		onDismissRequest = onDismissRequest,
		icon = { Icon(Icons.Outlined.PlaylistAdd, null) },
		title = { Text(stringResource(Res.string.action_add_to_playlist)) },
		action = {
			IconButton(
				onClick = {
					ctx.clickSound()
					viewModel.refreshResults()
				},
				enabled = state !is UiState.Loading,
				content = {
					Icon(
						Icons.Outlined.Refresh,
						contentDescription = stringResource(Res.string.action_refresh)
					)
				}
			)
		},
		buttons = {
			if ((state as? UiState.Success)?.data?.isNotEmpty() == true
				|| state is UiState.Loading
			) {
				FormButton(
					onClick = {
						ctx.clickSound()
						viewModel.confirm()
					},
					enabled = confirmState !is UiState.Loading && selectedPlaylist != null,
					color = MaterialTheme.colorScheme.primary
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
				FormButton(
					onClick = {
						ctx.clickSound()
						createDialogShown = true
					},
					content = { Text(stringResource(Res.string.action_new)) }
				)
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
			when (val state = state) {
				is UiState.Loading -> Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.Center
				) {
					ContainedLoadingIndicator(Modifier.size(48.dp))
				}

				is UiState.Error -> Box(
					modifier = Modifier.fillMaxWidth(),
					contentAlignment = Alignment.Center
				) {
					ErrorBox(state)
				}

				is UiState.Success -> {
					val playlists = state.data
					if (playlists.isNotEmpty()) {
						list(playlists)
					} else {
						Text(
							stringResource(
								if (playlistToExclude != null)
									Res.string.info_no_other_playlists
								else Res.string.info_no_playlists
							)
						)
					}
				}
			}
		}
	)

	if (createDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistCreateDialog(
			navigateAfterwards = false,
			onDismissRequest = { createDialogShown = false },
			onRefresh = { viewModel.refreshResults() }
		)
	}
}
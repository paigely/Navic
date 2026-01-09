package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.ios_share
import navic.composeapp.generated.resources.playlist_remove
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Tracks
import paige.navic.ui.component.ArtGrid
import paige.navic.ui.component.ArtGridItem
import paige.navic.ui.component.ArtGridPlaceholder
import paige.navic.ui.component.ErrorBox
import paige.navic.ui.component.Form
import paige.navic.ui.component.FormRow
import paige.navic.ui.component.RefreshBox
import paige.navic.ui.component.dialog.DeletionDialog
import paige.navic.ui.component.dialog.DeletionEndpoint
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.viewmodel.PlaylistsViewModel
import paige.navic.util.UiState
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistsScreen(
	viewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel() }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val haptics = LocalHapticFeedback.current

	val playlistsState by viewModel.playlistsState.collectAsState()
	val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()

	var deletionId by remember { mutableStateOf<String?>(null) }

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	RefreshBox(
		modifier = Modifier.background(MaterialTheme.colorScheme.surface),
		isRefreshing = playlistsState is UiState.Loading,
		onRefresh = { viewModel.refreshPlaylists() }
	) {
		when (playlistsState) {
			is UiState.Loading -> ArtGridPlaceholder()
			is UiState.Success -> {
				val playlists = (playlistsState as UiState.Success).data
				ArtGrid {
					items(playlists) { playlist ->
						ArtGridItem(
							modifier = Modifier.combinedClickable(
								onClick = {
									ctx.clickSound()
									backStack.add(Tracks(playlist))
								},
								onLongClick = {
									haptics.performHapticFeedback(HapticFeedbackType.LongPress)
									viewModel.selectPlaylist(playlist)
								}
							),
							imageUrl = playlist.coverArt,
							title = playlist.name,
							subtitle = buildString {
								append(
									pluralStringResource(
										Res.plurals.count_songs,
										playlist.songCount,
										playlist.songCount
									) + "\n"
								)
								playlist.comment?.let {
									append("${playlist.comment}\n")
								}
							}
						)
					}
				}
			}

			is UiState.Error -> ErrorBox(playlistsState as UiState.Error)
		}
	}

	selectedPlaylist?.let {
		ModalBottomSheet(
			onDismissRequest = { viewModel.clearSelection() }
		) {
			Form(modifier = Modifier.padding(14.dp)) {
				FormRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					onClick = {
						shareId = selectedPlaylist?.id
						viewModel.clearSelection()
					},
				) {
					Icon(
						vectorResource(Res.drawable.ios_share),
						contentDescription = null
					)
					Text(stringResource(Res.string.action_share))
				}
				FormRow(
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					onClick = {
						deletionId = selectedPlaylist?.id
						viewModel.clearSelection()
					},
				) {
					Icon(
						vectorResource(Res.drawable.playlist_remove),
						contentDescription = null
					)
					Text(stringResource(Res.string.action_delete))
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)

	@Suppress("AssignedValueIsNeverRead")
	DeletionDialog(
		endpoint = DeletionEndpoint.PLAYLIST,
		id = deletionId,
		onIdClear = { deletionId = null }
	)
}

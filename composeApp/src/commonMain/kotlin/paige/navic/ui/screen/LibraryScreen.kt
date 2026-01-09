package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_artist
import navic.composeapp.generated.resources.share
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Tracks
import paige.navic.ui.component.ArtGrid
import paige.navic.ui.component.ArtGridItem
import paige.navic.ui.component.ArtGridPlaceholder
import paige.navic.ui.component.Dropdown
import paige.navic.ui.component.DropdownItem
import paige.navic.ui.component.ErrorBox
import paige.navic.ui.component.RefreshBox
import paige.navic.ui.component.dialog.ShareDialog
import paige.navic.ui.viewmodel.LibraryViewModel
import paige.navic.util.UiState
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = viewModel { LibraryViewModel() }) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val haptics = LocalHapticFeedback.current

	val albumsState by viewModel.albumsState.collectAsState()
	val selection by viewModel.selectedAlbum.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val starredState by viewModel.starredState.collectAsState()

	RefreshBox(
		modifier = Modifier.background(MaterialTheme.colorScheme.surface),
		isRefreshing = albumsState is UiState.Loading,
		onRefresh = { viewModel.refreshAlbums() }
	) {
		AnimatedContent(albumsState) {
			when (it) {
				is UiState.Loading -> ArtGridPlaceholder()
				is UiState.Success -> ArtGrid {
					items(it.data) { album ->
						Box {
							ArtGridItem(
								modifier = Modifier.combinedClickable(
									onClick = {
										ctx.clickSound()
										backStack.add(Tracks(album))
									},
									onLongClick = {
										haptics.performHapticFeedback(HapticFeedbackType.LongPress)
										viewModel.selectAlbum(album)
									}
								),
								imageUrl = album.coverArt,
								title = album.name
									?: stringResource(Res.string.info_unknown_album),
								subtitle = (album.artist
									?: stringResource(Res.string.info_unknown_artist)) + "\n",
							)
							Dropdown(
								expanded = selection == album,
								onDismissRequest = {
									viewModel.clearSelection()
								}
							) {
								DropdownItem(
									text = Res.string.action_share,
									leadingIcon = Res.drawable.share,
									onClick = {
										shareId = album.id
										viewModel.clearSelection()
									},
								)
								val starred =
									(starredState as? UiState.Success)?.data
								DropdownItem(
									text = if (starred == true)
										Res.string.action_remove_star
									else Res.string.action_star,
									leadingIcon = Res.drawable.unstar,
									onClick = {
										if (starred == true)
											viewModel.unstarSelectedAlbum()
										else viewModel.starSelectedAlbum()
										viewModel.clearSelection()
									},
									enabled = starred != null
								)
							}
						}
					}
				}

				is UiState.Error -> ErrorBox(it)
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
}

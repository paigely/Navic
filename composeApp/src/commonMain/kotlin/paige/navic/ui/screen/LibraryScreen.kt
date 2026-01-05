package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import navic.composeapp.generated.resources.ios_share
import navic.composeapp.generated.resources.star
import navic.composeapp.generated.resources.unstar
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
	val selectedAlbum by viewModel.selectedAlbum.collectAsState()

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
				is UiState.Success -> {
					val albums = it.data
					ArtGrid {
						items(albums) { album ->
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
								title = album.name ?: "Unknown album",
								subtitle = (album.artist ?: "Unknown album") + "\n",
							)
						}
					}
				}

				is UiState.Error -> ErrorBox(it)
			}
		}
	}

	selectedAlbum?.let {
		ModalBottomSheet(
			onDismissRequest = { viewModel.clearSelection() }
		) {
			when (starredState) {
				is UiState.Loading -> Row(
					Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.Center
				) {
					CircularProgressIndicator()
				}
				is UiState.Error,
				is UiState.Success -> {
					Form(modifier = Modifier.padding(14.dp)) {
						FormRow(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							onClick = {
								shareId = selectedAlbum?.id
								viewModel.clearSelection()
							},
						) {
							Icon(
								vectorResource(Res.drawable.ios_share),
								contentDescription = null
							)
							Text("Share")
						}
						(starredState as? UiState.Success)?.data?.let { starred ->
							FormRow(
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								onClick = {
									if (starred) {
										viewModel.unstarSelectedAlbum()
									} else {
										viewModel.starSelectedAlbum()
									}
									viewModel.clearSelection()
								},
							) {
								Icon(
									vectorResource(
										if (starred)
											Res.drawable.unstar
										else Res.drawable.star
									),
									contentDescription = null
								)
								Text(if (starred) "Unstar" else "Star")
							}
						}
					}
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
}


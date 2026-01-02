package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.viewmodel.compose.viewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Tracks
import paige.navic.ui.component.ArtGrid
import paige.navic.ui.component.ArtGridItem
import paige.navic.ui.component.ArtGridPlaceholder
import paige.navic.ui.component.ErrorBox
import paige.navic.ui.component.RefreshBox
import paige.navic.ui.viewmodel.LibraryViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.toAny

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LibraryScreen(viewModel: LibraryViewModel = viewModel { LibraryViewModel() }) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val haptics = LocalHapticFeedback.current
	val albumsState by viewModel.albumsState.collectAsState()

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
					if (!albums.isEmpty()) {
						ArtGrid {
							items(albums) { album ->
								ArtGridItem(
									modifier = Modifier.combinedClickable(
										onClick = {
											ctx.clickSound()
											backStack.add(Tracks(tracks = album.toAny()))
										},
										onLongClick = {
											haptics.performHapticFeedback(HapticFeedbackType.LongPress)
										}
									),
									imageUrl = album.coverArt,
									title = album.name ?: "Unknown album",
									subtitle = (album.artist ?: "Unknown album") + "\n",
								)
							}
						}
					} else {
						ArtGridPlaceholder()
					}
				}

				is UiState.Error -> ErrorBox(it)
			}
		}
	}
}

package paige.navic.ui.screens.album.components

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_albums
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainAlbum
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Album
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.utils.UiState

fun LazyGridScope.albumListScreenContent(
	state: UiState<List<DomainAlbum>>,
	starred: Boolean,
	selectedAlbum: DomainAlbum?,
	onUpdateSelection: (DomainAlbum) -> Unit,
	onClearSelection: () -> Unit,
	onSetShareId: (String) -> Unit,
	onSetStarred: (Boolean) -> Unit,
	isOnline: Boolean
) {
	val data = state.data.orEmpty()
	if (data.isNotEmpty()) {
		items(data, { it.id }) { album ->
			AlbumListScreenItem(
				modifier = Modifier.animateItem(),
				tab = "albums",
				album = album,
				selected = album == selectedAlbum,
				starred = starred,
				onSelect = { onUpdateSelection(album) },
				onDeselect = { onClearSelection() },
				onSetStarred = { onSetStarred(it) },
				onSetShareId = onSetShareId,
				isOnline = isOnline
			)
		}
	} else {
		when (state) {
			is UiState.Loading -> {
				artGridPlaceholder()
			}

			else -> {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						icon = Icons.Outlined.Album,
						label = stringResource(Res.string.info_no_albums)
					)
				}
			}
		}
	}
}

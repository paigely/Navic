package paige.navic.ui.screens.album.components

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_albums
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainAlbum
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Album
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.artGridPlaceholder

fun LazyGridScope.albumListScreenContent(
	pagedAlbums: LazyPagingItems<DomainAlbum>,
	starred: Boolean,
	selectedAlbum: DomainAlbum?,
	onUpdateSelection: (DomainAlbum) -> Unit,
	onClearSelection: () -> Unit,
	onSetShareId: (String) -> Unit,
	onSetStarred: (Boolean) -> Unit,
	onAddToQueue: () -> Unit,
	isOnline: Boolean
) {
	val refreshState = pagedAlbums.loadState.refresh

	if (refreshState is LoadState.Loading && pagedAlbums.itemCount == 0) {
		artGridPlaceholder()
		return
	}

	if (refreshState is LoadState.NotLoading && pagedAlbums.itemCount == 0) {
		item(span = { GridItemSpan(maxLineSpan) }) {
			ContentUnavailable(
				icon = Icons.Outlined.Album,
				label = stringResource(Res.string.info_no_albums)
			)
		}
		return
	}

	items(
		count = pagedAlbums.itemCount,
		key = pagedAlbums.itemKey { it.id },
		contentType = pagedAlbums.itemContentType { "Album" }
	) { index ->
		val album = pagedAlbums[index]
		if (album != null) {
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
				onAddToQueue = onAddToQueue,
				isOnline = isOnline
			)
		}
	}
}

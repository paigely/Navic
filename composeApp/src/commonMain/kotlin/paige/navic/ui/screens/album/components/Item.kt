package paige.navic.ui.screens.album.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog

@Composable
fun AlbumListScreenItem(
	modifier: Modifier = Modifier,
	tab: String,
	album: DomainAlbum,
	selected: Boolean,
	starred: Boolean,
	rating: Int,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit,
	onSetShareId: (String) -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	onSetRating: (Int) -> Unit,
	isOnline: Boolean
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val scope = rememberCoroutineScope()

	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Box(modifier) {
		ArtGridItem(
			onClick = {
				ctx.clickSound()
				scope.launch {
					backStack.add(Screen.CollectionDetail(album.id, tab))
				}
			},
			onLongClick = onSelect,
			coverArtId = album.coverArtId,
			title = album.name,
			subtitle = album.artistName,
			id = album.id,
			tab = tab
		)
		if (selected) {
			CollectionSheet(
				onDismissRequest = onDeselect,
				collection = album,
				isOnline = isOnline,
				onShare = { onSetShareId(album.id) },
				onPlayNext = onPlayNext,
				onAddToQueue = onAddToQueue,
				starred = starred,
				onSetStarred = onSetStarred,
				onAddAllToPlaylist = { playlistDialogShown = true },
				rating = rating,
				onSetRating = onSetRating
			)
		}

		if (playlistDialogShown) {
			@Suppress("AssignedValueIsNeverRead")
			PlaylistUpdateDialog(
				songs = album.songs.orEmpty().toPersistentList(),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	}
}

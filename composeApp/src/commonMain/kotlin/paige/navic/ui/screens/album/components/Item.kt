package paige.navic.ui.screens.album.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import paige.navic.LocalPlatformContext
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.ui.navigation.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumSummary
import paige.navic.domain.manager.DownloadManager
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.notice_download_started
import navic.composeapp.generated.resources.notice_deleted_download
import paige.navic.domain.manager.SnackBarManager

@Composable
fun AlbumListScreenItem(
	modifier: Modifier = Modifier,
	tab: String,
	album: DomainAlbumSummary,
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
	selectedAlbum: DomainAlbum? = null
) {
	val platformContext = LocalPlatformContext.current
	val backStack = LocalNavStack.current
	val snackBarManager = koinInject<SnackBarManager>()
	val scope = rememberCoroutineScope()

	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	val downloadManager = koinInject<DownloadManager>()
	val downloadStatus by if (selected && selectedAlbum != null) {
		downloadManager
			.getCollectionDownloadStatus(selectedAlbum.songs.map { it.id })
			.collectAsState(initial = DownloadStatus.NOT_DOWNLOADED)
	} else {
		mutableStateOf(DownloadStatus.NOT_DOWNLOADED)
	}

	Box(modifier) {
		ArtGridItem(
			onClick = dropUnlessResumed {
				platformContext.clickSound()
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
		if (selected && selectedAlbum != null) {
			CollectionSheet(
				onDismissRequest = onDeselect,
				collection = selectedAlbum,
				onShare = { onSetShareId(selectedAlbum.id) },
				onPlayNext = onPlayNext,
				onAddToQueue = onAddToQueue,
				downloadStatus = downloadStatus,
				onDownloadAll = { 
					scope.launch {
						downloadManager.downloadCollection(selectedAlbum)
						snackBarManager.notify(Res.string.notice_download_started)
					}
				},
				onCancelDownloadAll = {
					scope.launch {
						selectedAlbum.songs.forEach { downloadManager.cancelDownload(it.id) }
					}
				},
				onDeleteDownloadAll = {
					scope.launch {
						downloadManager.deleteDownloadedCollection(selectedAlbum)
						snackBarManager.notify(Res.string.notice_deleted_download)
					}
				},
				starred = starred,
				onSetStarred = onSetStarred,
				onAddAllToPlaylist = { playlistDialogShown = true },
				onViewArtist = dropUnlessResumed {
					backStack.add(Screen.ArtistDetail(selectedAlbum.artistId))
				},
				rating = rating,
				onSetRating = onSetRating
			)
		}

		if (playlistDialogShown && selectedAlbum != null) {
			PlaylistUpdateDialog(
				songs = selectedAlbum.songs.toPersistentList(),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	}
}

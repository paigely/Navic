package paige.navic.ui.screens.collection.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.dialogs.QueueDuplicateDialog
import paige.navic.ui.components.sheets.SongSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog

@Composable
fun CollectionDetailScreenSongRowDropdown(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	onRemoveStar: () -> Unit,
	onAddStar: () -> Unit,
	onShare: () -> Unit,
	collection: DomainSongCollection,
	song: DomainSong,
	onRemoveFromPlaylist: () -> Unit,
	starred: Boolean,
	downloadStatus: DownloadStatus?,
	isOnline: Boolean,
	onDownload: () -> Unit,
	onCancelDownload: () -> Unit,
	onDeleteDownload: () -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	rating: Int,
	onSetRating: (Int) -> Unit
) {
	val player = koinViewModel<MediaPlayerViewModel>()
	val backStack = LocalNavStack.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }
	var duplicateQueueDialogShown by rememberSaveable { mutableStateOf(false) }

	if (expanded) {
		SongSheet(
			onDismissRequest = onDismissRequest,
			song = song,
			collection = collection,
			starred = starred,
			onSetStarred = { starred ->
				if (starred) onAddStar() else onRemoveStar()
			},
			onShare = onShare,
			onPlayNext = {
				if (player.uiState.value.queue.any { it.id == song.id }) {
					duplicateQueueDialogShown = true
				} else {
					onPlayNext()
				}
			},
			onAddToQueue = {
				if (player.uiState.value.queue.any { it.id == song.id }) {
					duplicateQueueDialogShown = true
				} else {
					onAddToQueue()
				}
			},
			onTrackInfo = {
				backStack.add(Screen.SongDetail(song.id))
			},
			onViewAlbum = if (collection !is DomainAlbum && song.albumId != null) {
				{
					backStack.add(
						Screen.CollectionDetail(
							collectionId = song.albumId,
							tab = "library"
						)
					)
				}
			} else null,
			onViewArtist = {
				backStack.add(Screen.ArtistDetail(song.artistId))
			},
			onAddToPlaylist = {
				playlistDialogShown = true
			},
			onRemoveFromPlaylist = onRemoveFromPlaylist,
			downloadStatus = downloadStatus,
			onDownload = onDownload,
			onCancelDownload = onCancelDownload,
			onDeleteDownload = onDeleteDownload,
			rating = rating,
			onSetRating = onSetRating
		)
	}

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = persistentListOf(song),
			playlistToExclude = if (collection is DomainPlaylist)
				collection.id
			else null,
			onDismissRequest = { playlistDialogShown = false }
		)
	}

	if (duplicateQueueDialogShown) {
		QueueDuplicateDialog(
			onDismissRequest = {
				duplicateQueueDialogShown = false
				onDismissRequest()
			},
			onConfirm = {
				onAddToQueue()
			}
		)
	}
}

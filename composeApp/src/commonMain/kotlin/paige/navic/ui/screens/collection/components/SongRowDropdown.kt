package paige.navic.ui.screens.collection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_another_playlist
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_add_to_queue
import navic.composeapp.generated.resources.action_cancel_download
import navic.composeapp.generated.resources.action_delete_download
import navic.composeapp.generated.resources.action_download
import navic.composeapp.generated.resources.action_remove_from_playlist
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.action_track_info
import navic.composeapp.generated.resources.info_click_to_retry
import navic.composeapp.generated.resources.info_download_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.DownloadOff
import paige.navic.icons.outlined.Info
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Queue
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.dialogs.QueueDuplicateDialog
import paige.navic.ui.components.sheets.SongSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.UiState

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
    starredState: UiState<Boolean>,
    downloadStatus: DownloadStatus?,
	isOnline: Boolean,
    onDownload: () -> Unit,
    onCancelDownload: () -> Unit,
    onDeleteDownload: () -> Unit,
    onAddToQueue: () -> Unit,
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
			starred = (starredState as? UiState.Success)?.data,
			onSetStarred = { starred ->
				if (starred) onAddStar() else onRemoveStar()
			},
			onShare = onShare,
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
			onAddToPlaylist = {
				playlistDialogShown = true
			},
			onRemoveFromPlaylist = onRemoveFromPlaylist,
			downloadStatus = downloadStatus,
			isOnline = isOnline,
			onDownload = onDownload,
			onCancelDownload = onCancelDownload,
			onDeleteDownload = onDeleteDownload
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

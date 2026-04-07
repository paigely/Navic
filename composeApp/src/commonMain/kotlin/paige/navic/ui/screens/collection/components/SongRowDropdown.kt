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
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
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
	val backStack = LocalNavStack.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Dropdown(
		expanded = expanded,
		onDismissRequest = onDismissRequest
	) {
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = { Text(stringResource(Res.string.action_add_to_queue)) },
			leadingIcon = { Icon(Icons.Outlined.Queue, null) },
			onClick = {
				onAddToQueue()
				onDismissRequest()
			},
		)
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = { Text(stringResource(Res.string.action_share)) },
			leadingIcon = { Icon(Icons.Outlined.Share, null) },
			onClick = {
				onShare()
				onDismissRequest()
			},
		)
		val starred =
			(starredState as? UiState.Success)?.data
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = {
				Text(
					stringResource(
						if (starred == true)
							Res.string.action_remove_star
						else Res.string.action_star
					)
				)
			},
			leadingIcon = {
				Icon(
					if (starred == true)
						Icons.Filled.Star
					else Icons.Outlined.Star,
					null
				)
			},
			onClick = {
				if (starred == true)
					onRemoveStar()
				else onAddStar()
				onDismissRequest()
			},
			enabled = starred != null
		)

		when (downloadStatus) {
			DownloadStatus.DOWNLOADING -> {
				DropdownItem(
					containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
					text = { Text(stringResource(Res.string.action_cancel_download)) },
					leadingIcon = { Icon(Icons.Outlined.Close, null) },
					onClick = {
						onCancelDownload()
						onDismissRequest()
					}
				)
			}
			DownloadStatus.DOWNLOADED -> {
				DropdownItem(
					containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
					text = { Text(stringResource(Res.string.action_delete_download)) },
					leadingIcon = { Icon(Icons.Outlined.Delete, null) },
					onClick = {
						onDeleteDownload()
						onDismissRequest()
					}
				)
			}
			DownloadStatus.FAILED -> {
				DropdownItem(
					containerColor = MaterialTheme.colorScheme.errorContainer,
					text = {
						Column {
							Text(
								text = stringResource(Res.string.info_download_failed),
								color = MaterialTheme.colorScheme.error
							)
							Text(
								text = stringResource(Res.string.info_click_to_retry),
								color = MaterialTheme.colorScheme.error,
								style = MaterialTheme.typography.labelSmall
							)
						}
					},
					leadingIcon = { Icon(Icons.Outlined.DownloadOff, null, tint = MaterialTheme.colorScheme.error) },
					onClick = {
						onDownload()
						onDismissRequest()
					}
				)
			}
			else -> {
				DropdownItem(
					containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
					text = { Text(stringResource(Res.string.action_download)) },
					leadingIcon = { Icon(Icons.Outlined.Download, null) },
					onClick = {
						onDownload()
						onDismissRequest()
					},
					enabled = isOnline
				)
			}
		}

		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = { Text(stringResource(Res.string.action_track_info)) },
			leadingIcon = { Icon(Icons.Outlined.Info, null) },
			onClick = {
				backStack.add(Screen.SongDetail(song.id))
				onDismissRequest()
			},
		)
		DropdownItem(
			containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
			text = {
				Text(
					stringResource(
						if (collection !is DomainAlbum)
							Res.string.action_add_to_another_playlist
						else Res.string.action_add_to_playlist
					)
				)
			},
			leadingIcon = {
				Icon(
					Icons.Outlined.PlaylistAdd,
					null
				)
			},
			onClick = {
				onDismissRequest()
				playlistDialogShown = true
			},
		)
		if (collection !is DomainAlbum) {
			DropdownItem(
				containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
				text = { Text(stringResource(Res.string.action_remove_from_playlist)) },
				leadingIcon = {
					Icon(
						Icons.Outlined.PlaylistRemove,
						null
					)
				},
				onClick = {
					onRemoveFromPlaylist()
				},
			)
		}
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
}

package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
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
import navic.composeapp.generated.resources.action_view_album
import navic.composeapp.generated.resources.info_click_to_retry
import navic.composeapp.generated.resources.info_download_failed
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainAlbum
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
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongSheet(
	onDismissRequest: () -> Unit,
	song: DomainSong,
	collection: DomainSongCollection? = null,
	starred: Boolean? = null,
	onSetStarred: ((Boolean) -> Unit)? = null,
	onShare: (() -> Unit)? = null,
	onAddToQueue: (() -> Unit)? = null,
	onTrackInfo: (() -> Unit)? = null,
	onViewAlbum: (() -> Unit)? = null,
	onAddToPlaylist: (() -> Unit)? = null,
	onRemoveFromPlaylist: (() -> Unit)? = null,
	downloadStatus: DownloadStatus? = null,
	isOnline: Boolean = true,
	onDownload: (() -> Unit)? = null,
	onCancelDownload: (() -> Unit)? = null,
	onDeleteDownload: (() -> Unit)? = null,
) {
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
	) {
		ListItem(
			headlineContent = { Text(song.title) },
			supportingContent = {
				Text(
					"${song.albumTitle ?: ""} • ${song.artistName} • ${song.year ?: ""}"
				)
			},
			leadingContent = {
				CoverArt(
					coverArtId = song.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
				)
			},
			colors = ListItemDefaults.colors(containerColor = Color.Transparent)
		)

		Form(modifier = Modifier.padding(16.dp)) {
			if (onAddToQueue != null) {
				FormRow(
					onClick = {
						onAddToQueue()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.Queue, null)
					Text(
						stringResource(Res.string.action_add_to_queue),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (onShare != null) {
				FormRow(
					onClick = {
						onShare()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.Share, null)
					Text(
						stringResource(Res.string.action_share),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (starred != null && onSetStarred != null) {
				FormRow(
					onClick = {
						onSetStarred(!starred)
						onDismissRequest()
					}
				) {
					Icon(if (starred) Icons.Filled.Star else Icons.Outlined.Star, null)
					Text(
						stringResource(if (starred) Res.string.action_remove_star else Res.string.action_star),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (downloadStatus != null) {
				when (downloadStatus) {
					DownloadStatus.DOWNLOADING -> {
						FormRow(
							onClick = {
								onCancelDownload?.invoke()
								onDismissRequest()
							}
						) {
							Icon(Icons.Outlined.Close, null)
							Text(
								stringResource(Res.string.action_cancel_download),
								modifier = Modifier.padding(start = 12.dp).weight(1f)
							)
						}
					}
					DownloadStatus.DOWNLOADED -> {
						FormRow(
							onClick = {
								onDeleteDownload?.invoke()
								onDismissRequest()
							}
						) {
							Icon(Icons.Outlined.Delete, null)
							Text(
								stringResource(Res.string.action_delete_download),
								modifier = Modifier.padding(start = 12.dp).weight(1f)
							)
						}
					}
					DownloadStatus.FAILED -> {
						FormRow(
							onClick = {
								onDownload?.invoke()
								onDismissRequest()
							}
						) {
							Icon(Icons.Outlined.DownloadOff, null, tint = MaterialTheme.colorScheme.error)
							Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
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
						}
					}
					else -> {
						FormRow(
							onClick = {
								if (isOnline) {
									onDownload?.invoke()
									onDismissRequest()
								}
							},
							modifier = Modifier.alpha(if (isOnline) 1f else 0.5f)
						) {
							Icon(Icons.Outlined.Download, null)
							Text(
								stringResource(Res.string.action_download),
								modifier = Modifier.padding(start = 12.dp).weight(1f)
							)
						}
					}
				}
			} else if (onDownload != null) {
				FormRow(
					onClick = {
						if (isOnline) {
							onDownload()
							onDismissRequest()
						}
					},
					modifier = Modifier.alpha(if (isOnline) 1f else 0.5f)
				) {
					Icon(Icons.Outlined.Download, null)
					Text(
						stringResource(Res.string.action_download),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (onTrackInfo != null) {
				FormRow(
					onClick = {
						onTrackInfo()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.Info, null)
					Text(
						stringResource(Res.string.action_track_info),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (onViewAlbum != null) {
				FormRow(
					onClick = {
						onViewAlbum()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.Album, null)
					Text(
						stringResource(Res.string.action_view_album),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (onAddToPlaylist != null) {
				FormRow(
					onClick = {
						onAddToPlaylist()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.PlaylistAdd, null)
					Text(
						stringResource(
							if (collection != null && collection !is DomainAlbum)
								Res.string.action_add_to_another_playlist
							else Res.string.action_add_to_playlist
						),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (onRemoveFromPlaylist != null && collection != null && collection !is DomainAlbum) {
				FormRow(
					onClick = {
						onRemoveFromPlaylist()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.PlaylistRemove, null)
					Text(
						stringResource(Res.string.action_remove_from_playlist),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}
		}
	}
}

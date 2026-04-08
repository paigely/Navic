package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_all_to_playlist
import navic.composeapp.generated.resources.action_cancel_download
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_download
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.action_view_on_lastfm
import navic.composeapp.generated.resources.action_view_on_musicbrainz
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumInfo
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.brand.Lastfm
import paige.navic.icons.brand.Musicbrainz
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSheet(
	onDismissRequest: () -> Unit,
	collection: DomainSongCollection?,
	albumInfo: DomainAlbumInfo? = null,
	isOnline: Boolean,
	onDownloadAll: (() -> Unit)? = null,
	onCancelDownloadAll: (() -> Unit)? = null,
	downloadStatus: DownloadStatus? = null,
	onShare: (() -> Unit)? = null,
	onAddAllToPlaylist: (() -> Unit)? = null,
	onViewOnLastFm: ((String) -> Unit)? = null,
	onViewOnMusicBrainz: ((String) -> Unit)? = null,
	starred: Boolean? = null,
	onSetStarred: ((Boolean) -> Unit)? = null,
	onDelete: (() -> Unit)? = null,
) {
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
	) {
		ListItem(
			leadingContent = {
				CoverArt(
					coverArtId = collection?.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
				)
			},
			headlineContent = { Text(collection?.name.orEmpty()) },
			supportingContent = {
				Text(
					listOfNotNull(
						collection?.name,
						(collection as? DomainAlbum)?.artistName,
						(collection as? DomainPlaylist)?.comment,
						(collection as? DomainAlbum)?.genre,
						(collection as? DomainAlbum)?.year
					).joinToString(" • ")
				)
			},
			colors = ListItemDefaults.colors(containerColor = Color.Transparent)
		)

		Form(modifier = Modifier.padding(16.dp)) {
			if (onViewOnLastFm != null && albumInfo?.lastFmUrl != null) {
				FormRow(
					onClick = {
						onViewOnLastFm(albumInfo.lastFmUrl)
						onDismissRequest()
					}
				) {
					Icon(Icons.Brand.Lastfm, null)
					Text(
						stringResource(Res.string.action_view_on_lastfm),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}

			if (onViewOnMusicBrainz != null && albumInfo?.musicBrainzId != null) {
				FormRow(
					onClick = {
						onViewOnMusicBrainz(albumInfo.musicBrainzId)
						onDismissRequest()
					}
				) {
					Icon(Icons.Brand.Musicbrainz, null)
					Text(
						stringResource(Res.string.action_view_on_musicbrainz),
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

			if (onAddAllToPlaylist != null) {
				FormRow(
					onClick = {
						onAddAllToPlaylist()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.PlaylistAdd, null)
					Text(
						stringResource(Res.string.action_add_all_to_playlist),
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

			if (onDownloadAll != null && onCancelDownloadAll != null && downloadStatus != null) {
				val downloading = downloadStatus === DownloadStatus.DOWNLOADING
				val enabled = isOnline && collection?.songs.orEmpty().isNotEmpty()

				FormRow(
					onClick = {
						if (enabled) {
							if (!downloading) {
								onDownloadAll()
							} else {
								onCancelDownloadAll()
							}
							onDismissRequest()
						}
					},
					modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
				) {
					if (!downloading) {
						Icon(Icons.Outlined.Download, null)
						Text(
							stringResource(Res.string.action_download),
							modifier = Modifier.padding(start = 12.dp).weight(1f)
						)
					} else {
						CircularProgressIndicator(
							modifier = Modifier.size(20.dp),
							strokeWidth = 2.dp
						)
						Text(
							stringResource(Res.string.action_cancel_download),
							modifier = Modifier.padding(start = 12.dp).weight(1f)
						)
					}
				}
			}

			if (onDelete != null) {
				FormRow(
					onClick = {
						onDelete()
						onDismissRequest()
					}
				) {
					Icon(Icons.Outlined.PlaylistRemove, null)
					Text(
						stringResource(Res.string.action_delete),
						modifier = Modifier.padding(start = 12.dp).weight(1f)
					)
				}
			}
		}
	}
}

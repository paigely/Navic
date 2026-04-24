package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
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
import navic.composeapp.generated.resources.action_add_to_queue
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
import paige.navic.icons.outlined.Queue
import paige.navic.ui.components.common.CoverArt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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
	onAddToQueue: (() -> Unit)? = null,
	onAddAllToPlaylist: (() -> Unit)? = null,
	onViewOnLastFm: ((String) -> Unit)? = null,
	onViewOnMusicBrainz: ((String) -> Unit)? = null,
	starred: Boolean? = null,
	onSetStarred: ((Boolean) -> Unit)? = null,
	onDelete: (() -> Unit)? = null,
) {
	val contentPadding = PaddingValues(horizontal = 16.dp)
	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)
	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = null,
		contentWindowInsets = { BottomSheetDefaults.modalWindowInsets.add(WindowInsets(
			left = 8.dp,
			right = 8.dp
		)) }
	) {
		Spacer(Modifier.height(16.dp))

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
			colors = colors
		)

		HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

		if (onViewOnLastFm != null && albumInfo?.lastFmUrl != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_view_on_lastfm)) },
				leadingContent = { Icon(Icons.Brand.Lastfm, null) },
				onClick = {
					onViewOnLastFm(albumInfo.lastFmUrl)
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onViewOnMusicBrainz != null && albumInfo?.musicBrainzId != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_view_on_musicbrainz)) },
				leadingContent = { Icon(Icons.Brand.Musicbrainz, null) },
				onClick = {
					onViewOnMusicBrainz(albumInfo.musicBrainzId)
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onShare != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_share)) },
				leadingContent = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					onShare()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onAddToQueue != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_add_to_queue)) },
				leadingContent = { Icon(Icons.Outlined.Queue, null) },
				onClick = {
					onAddToQueue()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onAddAllToPlaylist != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_add_all_to_playlist)) },
				leadingContent = { Icon(Icons.Outlined.PlaylistAdd, null) },
				onClick = {
					onAddAllToPlaylist()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (starred != null && onSetStarred != null) {
			ListItem(
				content = {
					Text(stringResource(if (starred) Res.string.action_remove_star else Res.string.action_star))
				},
				leadingContent = {
					Icon(if (starred) Icons.Filled.Star else Icons.Outlined.Star, null)
				},
				onClick = {
					onSetStarred(!starred)
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onDownloadAll != null && onCancelDownloadAll != null && downloadStatus != null) {
			val downloading = downloadStatus === DownloadStatus.DOWNLOADING
			val enabled = isOnline && collection?.songs.orEmpty().isNotEmpty()

			ListItem(
				content = {
					Text(stringResource(if (!downloading) Res.string.action_download else Res.string.action_cancel_download))
				},
				leadingContent = {
					if (!downloading) {
						Icon(Icons.Outlined.Download, null)
					} else {
						CircularProgressIndicator(
							modifier = Modifier.size(20.dp),
							strokeWidth = 2.dp
						)
					}
				},
				modifier = Modifier
					.alpha(if (enabled) 1f else 0.5f),
				onClick = {
					if (!downloading) {
						onDownloadAll()
					} else {
						onCancelDownloadAll()
					}
					onDismissRequest()
				},
				enabled = enabled,
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onDelete != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_delete)) },
				leadingContent = { Icon(Icons.Outlined.PlaylistRemove, null) },
				onClick = {
					onDelete()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}
	}
}

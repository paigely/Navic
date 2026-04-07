package paige.navic.ui.screens.track.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toPersistentList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_all_to_playlist
import navic.composeapp.generated.resources.action_cancel_download
import navic.composeapp.generated.resources.action_download
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_view_on_lastfm
import navic.composeapp.generated.resources.action_view_on_musicbrainz
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainAlbumInfo
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.brand.Lastfm
import paige.navic.icons.brand.Musicbrainz
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.MoreVert
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.utils.UiState

@Composable
fun TracksScreenTopBar(
	collection: DomainSongCollection?,
	albumInfoState: UiState<DomainAlbumInfo>,
	scrolled: Boolean,
	onSetShareId: (shareId: String?) -> Unit,
	isOnline: Boolean,
	onDownloadAll: () -> Unit,
	onCancelDownloadAll: () -> Unit,
	downloadStatus: DownloadStatus
) {
	val uriHandler = LocalUriHandler.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	NestedTopBar(
		title = {
			AnimatedVisibility(
				scrolled,
				enter = scaleIn() + fadeIn(),
				exit = scaleOut() + fadeOut()
			) {
				Text(
					text = collection?.name.orEmpty(),
					maxLines = 1,
					overflow = TextOverflow.Ellipsis
				)
			}
		},
		actions = {
			Box {
				var expanded by remember { mutableStateOf(false) }
				TopBarButton({
					expanded = true
				}) {
					Icon(
						Icons.Outlined.MoreVert,
						stringResource(Res.string.action_more)
					)
				}
				Dropdown(
					expanded = expanded,
					onDismissRequest = { expanded = false }
				) {
					val info = (albumInfoState as? UiState.Success)?.data
					DropdownItem(
						text = { Text(stringResource(Res.string.action_view_on_lastfm)) },
						leadingIcon = { Icon(Icons.Brand.Lastfm, null) },
						enabled = albumInfoState is UiState.Success
							&& info?.lastFmUrl != null,
						onClick = {
							expanded = false
							info?.lastFmUrl?.let { url ->
								uriHandler.openUri(url)
							}
						}
					)
					DropdownItem(
						text = { Text(stringResource(Res.string.action_view_on_musicbrainz)) },
						leadingIcon = { Icon(Icons.Brand.Musicbrainz, null) },
						enabled = albumInfoState is UiState.Success
							&& info?.musicBrainzId != null,
						onClick = {
							expanded = false
							info?.musicBrainzId?.let { id ->
								uriHandler.openUri(
									"https://musicbrainz.org/release/$id"
								)
							}
						}
					)
					DropdownItem(
						text = { Text(stringResource(Res.string.action_share)) },
						leadingIcon = { Icon(Icons.Outlined.Share, null) },
						containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
						onClick = {
							expanded = false
							onSetShareId(collection?.id)
						},
					)
					DropdownItem(
						text = { Text(stringResource(Res.string.action_add_all_to_playlist)) },
						leadingIcon = { Icon(Icons.Outlined.PlaylistAdd, null) },
						containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
						onClick = {
							expanded = false
							playlistDialogShown = true
						},
					)
					val downloading = downloadStatus === DownloadStatus.DOWNLOADING
					DropdownItem(
						text = {
							if (!downloading) {
								Text(stringResource(Res.string.action_download))
							} else {
								Text(stringResource(Res.string.action_cancel_download))
							}
						},
						leadingIcon = {
							if (!downloading) {
								Icon(Icons.Outlined.Download, null)
							} else {
								CircularProgressIndicator(
									modifier = Modifier.size(20.dp),
									strokeWidth = 2.dp
								)
							}
						},
						containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
						enabled = isOnline,
						onClick = {
							if (!downloading) {
								onDownloadAll()
							} else {
								onCancelDownloadAll()
							}
						},
					)
				}
			}
		}
	)

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			tracks = collection?.songs.orEmpty().toPersistentList(),
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}

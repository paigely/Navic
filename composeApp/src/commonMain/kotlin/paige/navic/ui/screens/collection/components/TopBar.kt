package paige.navic.ui.screens.collection.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.toPersistentList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumInfo
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.outlined.MoreVert
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.UiState

@Composable
fun CollectionDetailScreenTopBar(
	collection: DomainSongCollection?,
	albumInfoState: UiState<DomainAlbumInfo>,
	titleAlpha: Float,
	onSetShareId: (shareId: String?) -> Unit,
	isOnline: Boolean,
	onDownloadAll: () -> Unit,
	onCancelDownloadAll: () -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	downloadStatus: DownloadStatus,
	rating: Int?,
	onSetRating: ((Int) -> Unit)?
) {
	val uriHandler = LocalUriHandler.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }
	val backStack = LocalNavStack.current

	NestedTopBar(
		title = {
			Text(
				text = collection?.name.orEmpty(),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis,
				modifier = Modifier.alpha(titleAlpha)
			)
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
				if (expanded) {
					CollectionSheet(
						onDismissRequest = { expanded = false },
						collection = collection,
						albumInfo = (albumInfoState as? UiState.Success)?.data,
						onDownloadAll = onDownloadAll,
						onCancelDownloadAll = onCancelDownloadAll,
						downloadStatus = downloadStatus,
						onShare = { onSetShareId(collection?.id) },
						onPlayNext = onPlayNext,
						onAddToQueue = onAddToQueue,
						onAddAllToPlaylist = { playlistDialogShown = true },
						onViewOnLastFm = { url -> uriHandler.openUri(url) },
						onViewOnMusicBrainz = { id ->
							uriHandler.openUri("https://musicbrainz.org/release/$id")
						},
						onViewArtist =
							if (collection is DomainAlbum)
								dropUnlessResumed { backStack.add(Screen.ArtistDetail(collection.artistId)) }
							else null,
						rating = rating,
						onSetRating = onSetRating
					)
				}
			}
		}
	)

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = collection?.songs.orEmpty().toPersistentList(),
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}

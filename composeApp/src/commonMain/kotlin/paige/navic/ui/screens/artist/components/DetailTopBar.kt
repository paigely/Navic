package paige.navic.ui.screens.artist.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.collections.immutable.toPersistentList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.icons.Icons
import paige.navic.icons.outlined.MoreVert
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.sheets.ArtistSheet
import paige.navic.ui.screens.artist.viewmodels.ArtistState
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.UiState

@Composable
fun ArtistDetailScreenTopBar(
	scrolled: Boolean,
	artistState: UiState<ArtistState>,
	starred: Boolean? = null,
	onSetStarred: ((Boolean) -> Unit)? = null,
) {
	val uriHandler = LocalUriHandler.current
	val state = (artistState as? UiState.Success)?.data
	val alpha by animateFloatAsState(
		if (scrolled) 1f else 0f
	)

	val player = koinViewModel<MediaPlayerViewModel>()

	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	if (state != null) {
		NestedTopBar(
			colors = TopAppBarDefaults.topAppBarColors(
				containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha)
			),
			title = {
				AnimatedVisibility(
					scrolled,
					enter = scaleIn() + fadeIn(),
					exit = scaleOut() + fadeOut()
				) {
					Text(state.artist.name)
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
					if (expanded) {
						ArtistSheet(
							onDismissRequest = { expanded = false },
							artist = state.artist,
							onPlayNext = {
								state.albums.reversed().forEach { album ->
									player.playNext(album)
								}
							},
							onAddToQueue = {
								state.albums.forEach { album ->
									player.addToQueue(album)
								}
							},
							onAddAllToPlaylist = {
								playlistDialogShown = true
							},
							onViewOnLastFm = { 
								expanded = false
								state.artist.lastFmUrl?.let { url ->
									uriHandler.openUri(url)
								}
							},
							onViewOnMusicBrainz = { 								
								expanded = false
								state.artist.musicBrainzId?.let { id ->
									uriHandler.openUri(
										"https://musicbrainz.org/artist/$id"
									)
								}
							},
							starred = starred,
							onSetStarred = onSetStarred
						)
					}
				}
			}
		)
		if (playlistDialogShown) {
			@Suppress("AssignedValueIsNeverRead")
			PlaylistUpdateDialog(
				songs = state.albums.flatMap { it.songs }.toPersistentList(),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	} else {
		NestedTopBar({})
	}
}

package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_track_info
import navic.composeapp.generated.resources.action_view_album
import navic.composeapp.generated.resources.action_view_artist
import navic.composeapp.generated.resources.action_view_playlist
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainPlaylist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Info
import paige.navic.icons.outlined.MoreHoriz
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog

@Composable
fun NowPlayingMoreButton() {
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val track = playerState.currentTrack
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Box {
		var expanded by remember { mutableStateOf(false) }
		IconButton(
			onClick = {
				ctx.clickSound()
				expanded = true
			},
			colors = IconButtonDefaults.filledTonalIconButtonColors(),
			modifier = Modifier.size(32.dp),
			enabled = playerState.currentTrack != null
		) {
			Icon(
				imageVector = Icons.Outlined.MoreHoriz,
				contentDescription = stringResource(Res.string.action_more)
			)
		}
		Dropdown(
			expanded = expanded,
			onDismissRequest = { expanded = false }
		) {
			DropdownItem(
				onClick = {
					playerState.currentCollection?.let { tracks ->
						expanded = false
						backStack.remove(Screen.NowPlaying)
						backStack.add(Screen.TrackList(tracks.id, ""))
					}
				},
				text = {
					Text(
						stringResource(
							when (playerState.currentCollection) {
								is DomainPlaylist -> Res.string.action_view_playlist
								else -> Res.string.action_view_album
							}
						)
					)
				},
				leadingIcon = { Icon(Icons.Outlined.Album, null) }
			)
			DropdownItem(
				onClick = {
					track?.artistId?.let { artistId ->
						expanded = false
						backStack.remove(Screen.NowPlaying)
						backStack.add(Screen.ArtistDetail(artistId))
					}
				},
				text = { Text(stringResource(Res.string.action_view_artist)) },
				leadingIcon = { Icon(Icons.Outlined.Artist, null) }
			)
			DropdownItem(
				onClick = {
					expanded = false
					playlistDialogShown = true
				},
				text = { Text(stringResource(Res.string.action_add_to_playlist)) },
				leadingIcon = { Icon(Icons.Outlined.PlaylistAdd, null) }
			)
			DropdownItem(
				onClick = {
					track?.id?.let { songId ->
						expanded = false
						backStack.remove(Screen.NowPlaying)
						backStack.add(Screen.TrackDetail(songId))
					}
				},
				text = { Text(stringResource(Res.string.action_track_info)) },
				leadingIcon = { Icon(Icons.Outlined.Info, null) }
			)
		}
	}

	if (playlistDialogShown && track != null) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			tracks = persistentListOf(track),
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}
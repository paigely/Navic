package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.MoreHoriz
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.sheets.SongSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.ui.theme.NavicTheme
import kotlin.time.Duration

@Composable
fun NowPlayingMoreButton(
	songRating: Int,
	onSetSongRating: (Int) -> Unit
) {
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val song = playerState.currentSong
	var expanded by remember { mutableStateOf(false) }
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	IconButton(
		onClick = {
			ctx.clickSound()
			expanded = true
		},
		colors = IconButtonDefaults.filledTonalIconButtonColors(),
		modifier = Modifier.size(32.dp),
		enabled = song != null
	) {
		Icon(
			imageVector = Icons.Outlined.MoreHoriz,
			contentDescription = stringResource(Res.string.action_more)
		)
	}

	if (expanded && song != null) {
		NavicTheme {
			SongSheet(
				onDismissRequest = { expanded = false },
				song = song,
				collection = playerState.currentCollection,
				onViewAlbum = dropUnlessResumed {
					playerState.currentCollection?.let { collection ->
						backStack.remove(Screen.NowPlaying)
						backStack.add(Screen.CollectionDetail(collection.id, ""))
					}
				},
				onViewArtist = dropUnlessResumed {
					backStack.remove(Screen.NowPlaying)
					backStack.add(Screen.ArtistDetail(song.artistId))
				},
				onShare = {
					shareId = song.id
				},
				onAddToPlaylist = {
					playlistDialogShown = true
				},
				onTrackInfo = dropUnlessResumed {
					backStack.remove(Screen.NowPlaying)
					backStack.add(Screen.SongDetail(song.id))
				},
				rating = songRating,
				onSetRating = onSetSongRating,
				showSleepTimer = true,
				showPlaybackSpeed = true
			)
		}
	}

	if (playlistDialogShown && song != null) {
		NavicTheme {
			@Suppress("AssignedValueIsNeverRead")
			PlaylistUpdateDialog(
				songs = persistentListOf(song),
				onDismissRequest = { playlistDialogShown = false }
			)
		}
	}

	NavicTheme {
		ShareDialog(
			id = shareId,
			onIdClear = { shareId = null },
			expiry = shareExpiry,
			onExpiryChange = { shareExpiry = it }
		)
	}
}

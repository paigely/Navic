package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_star
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Star
import paige.navic.shared.MediaPlayerViewModel

@Composable
fun NowPlayingStarButton(
	songIsStarred: Boolean,
	onSetSongIsStarred: (Boolean) -> Unit
) {
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	IconButton(
		onClick = {
			ctx.clickSound()
			onSetSongIsStarred(!songIsStarred)
		},
		colors = IconButtonDefaults.filledTonalIconButtonColors(),
		modifier = Modifier.size(32.dp),
		enabled = playerState.currentSong != null
	) {
		Icon(
			if (songIsStarred) Icons.Filled.Star else Icons.Outlined.Star,
			contentDescription = stringResource(Res.string.action_star)
		)
	}
}

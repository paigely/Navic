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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_star
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalMediaPlayer
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Star

@Composable
fun NowPlayingStarButton() {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	var isStarred by remember(playerState.currentTrack) {
		mutableStateOf(playerState.currentTrack?.starredAt != null)
	}
	val scope = rememberCoroutineScope()
	IconButton(
		onClick = {
			isStarred = !isStarred
			scope.launch {
				if (isStarred) player.starTrack() else player.unstarTrack()
			}
		},
		colors = IconButtonDefaults.filledTonalIconButtonColors(),
		modifier = Modifier.size(32.dp),
		enabled = playerState.currentTrack != null
	) {
		Icon(
			if (isStarred) Icons.Filled.Star else Icons.Outlined.Star,
			contentDescription = stringResource(Res.string.action_star)
		)
	}
}
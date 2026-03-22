package paige.navic.ui.screens.nowPlaying.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.zt64.subsonic.api.model.Song
import paige.navic.LocalMediaPlayer
import paige.navic.icons.Icons
import paige.navic.icons.filled.Note
import paige.navic.ui.components.common.CoverArt

@Composable
fun NowPlayingArtwork(
	modifier: Modifier = Modifier,
	isLandscape: Boolean,
	track: Song
) {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	val padding by animateDpAsState(
		targetValue = if (playerState.isPaused || playerState.currentTrack?.id !== track.id)
			48.dp
		else 16.dp
	)
	Box(
		contentAlignment = Alignment.Center,
		modifier = modifier
	) {
		CoverArt(
			coverArtId = track.coverArtId,
			modifier = Modifier
				.aspectRatio(1f)
				.then(if (isLandscape) Modifier.fillMaxHeight() else Modifier.fillMaxSize())
				.padding(padding),
			shadowElevation = 8.dp
		)
		if (track.coverArtId.isNullOrEmpty()) {
			Icon(
				imageVector = Icons.Filled.Note,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f),
				modifier = Modifier.size(96.dp)
			)
		}
	}
}

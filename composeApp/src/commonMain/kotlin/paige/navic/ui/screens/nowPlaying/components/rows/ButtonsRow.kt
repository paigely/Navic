package paige.navic.ui.screens.nowPlaying.components.rows

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.icons.Icons
import paige.navic.icons.filled.Pause
import paige.navic.icons.filled.Play
import paige.navic.icons.filled.RepeatOn
import paige.navic.icons.filled.ShuffleOn
import paige.navic.icons.filled.SkipNext
import paige.navic.icons.filled.SkipPrevious
import paige.navic.icons.outlined.Repeat
import paige.navic.icons.outlined.Shuffle
import paige.navic.ui.components.common.playPauseIconPainter

@Composable
fun NowPlayingButtonsRow() {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()
	val scale = remember { Animatable(1f) }
	val enabled = playerState.currentTrack != null

	LaunchedEffect(isPressed) {
		if (!isPressed) {
			if (scale.value != 1f) {
				scale.animateTo(
					targetValue = 1.2f,
					animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
				)
				scale.animateTo(
					targetValue = 1f,
					animationSpec = spring(
						dampingRatio = Spring.DampingRatioMediumBouncy,
						stiffness = Spring.StiffnessLow
					)
				)
			}
		} else {
			scale.animateTo(0.95f)
		}
	}

	Row(
		modifier = Modifier.widthIn(max = 400.dp),
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		IconButton(
			modifier = Modifier.weight(1f).aspectRatio(1f),
			onClick = { player.toggleShuffle() },
			enabled = enabled,
		) {
			Icon(
				imageVector = if (playerState.isShuffleEnabled)
					Icons.Filled.ShuffleOn
				else Icons.Outlined.Shuffle,
				contentDescription = null,
				modifier = Modifier.size(24.dp)
			)
		}
		IconButton(
			modifier = Modifier.weight(1f).aspectRatio(1f),
			onClick = { player.previous() },
			enabled = enabled
		) {
			Icon(
				imageVector = Icons.Filled.SkipPrevious,
				contentDescription = null,
				modifier = Modifier.size(32.dp)
			)
		}
		IconButton(
			modifier = Modifier
				.weight(1.3f)
				.aspectRatio(1f)
				.scale(scale.value)
				.clip(CircleShape)
				.indication(interactionSource, ripple(color = Color.Black)),
			colors = IconButtonDefaults.filledIconButtonColors(),
			onClick = {
				ctx.clickSound()
				player.togglePlay()
			},
			enabled = enabled,
			interactionSource = interactionSource
		) {
			val painter = playPauseIconPainter(playerState.isPaused)
			AnimatedContent(playerState.isLoading) { isBuffering ->
				if (!isBuffering) {
					if (painter != null) {
						Icon(
							painter = painter,
							contentDescription = null,
							modifier = Modifier.size(40.dp)
						)
					} else {
						Icon(
							imageVector = if (playerState.isPaused)
								Icons.Filled.Play
							else Icons.Filled.Pause,
							contentDescription = null,
							modifier = Modifier.size(40.dp)
						)
					}
				} else {
					CircularProgressIndicator(
						Modifier.size(40.dp),
						color = MaterialTheme.colorScheme.onPrimary,
						trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = .5f),
					)
				}
			}
		}
		IconButton(
			modifier = Modifier.weight(1f).aspectRatio(1f),
			onClick = {
				ctx.clickSound()
				player.next()
			},
			enabled = enabled,
		) {
			Icon(
				imageVector = Icons.Filled.SkipNext,
				contentDescription = null,
				modifier = Modifier.size(32.dp)
			)
		}
		IconButton(
			modifier = Modifier.weight(1f).aspectRatio(1f),
			onClick = {
				ctx.clickSound()
				player.toggleRepeat()
			},
			enabled = enabled,
		) {
			Icon(
				imageVector = when (playerState.repeatMode) {
					1 -> Icons.Filled.RepeatOn
					else -> Icons.Outlined.Repeat
				},
				contentDescription = null,
				modifier = Modifier.size(24.dp)
			)
		}
	}
}
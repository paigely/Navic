package paige.navic.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown_artist
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.data.models.Settings
import paige.navic.ui.components.common.BlendBackground
import paige.navic.ui.components.common.MarqueeText
import paige.navic.utils.rememberTrackPainter
import paige.subsonic.api.models.Track

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueScreen() {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val currentTrack = playerState.currentTrack
	val queue = playerState.queue
	val sharedPainter = rememberTrackPainter(currentTrack?.id, currentTrack?.coverArt)

	Box(modifier = Modifier.fillMaxSize()) {
		if (Settings.shared.animatePlayerBackground) {
			BlendBackground(
				painter = sharedPainter,
				isPaused = playerState.isPaused
			)
		}
		LazyColumn(
			modifier = Modifier.matchParentSize(),
			contentPadding = WindowInsets.statusBars.asPaddingValues()
				+ WindowInsets.systemBars.asPaddingValues()
				+ PaddingValues(vertical = 70.dp, horizontal = 16.dp),
			verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
		) {
			itemsIndexed(queue) { index, track ->
				QueueScreenItem(
					index = index,
					count = queue.count(),
					track = track,
					isPlaying = currentTrack?.id == track.id
						&& !playerState.isPaused,
					isSelected = currentTrack?.id == track.id,
					onClick = {
						ctx.clickSound()
						if (currentTrack?.id !== track.id) {
							player.playAt(index)
						}
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QueueScreenItem(
	index: Int,
	count: Int,
	track: Track,
	isPlaying: Boolean,
	isSelected: Boolean,
	onClick: () -> Unit
) {
	val color = MaterialTheme.colorScheme.surface.copy(
		alpha = if (isSelected) .7f else .5f
	)
	val contentColor = if (isSelected)
		MaterialTheme.colorScheme.primary
	else MaterialTheme.colorScheme.onSurface
	val supportingContentColor = if (isSelected)
		MaterialTheme.colorScheme.primary.copy(alpha = .7f)
	else MaterialTheme.colorScheme.onSurfaceVariant
	SegmentedListItem(
		onClick = onClick,
		colors = ListItemDefaults.colors(
			containerColor = color,
			selectedContainerColor = color,
			disabledContainerColor = color,
			draggedContainerColor = color,
			contentColor = contentColor,
			supportingContentColor = supportingContentColor
		),
		shapes = ListItemDefaults.segmentedShapes(
			index = index,
			count = count
		),
		verticalAlignment = Alignment.CenterVertically,
		content = {
			MarqueeText(track.title)
		},
		supportingContent = {
			MarqueeText(track.artist ?: stringResource(Res.string.info_unknown_artist))
		},
		leadingContent = {
			Text(
				"${index + 1}",
				modifier = Modifier.width(25.dp),
				style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
				fontWeight = FontWeight(400),
				fontSize = 13.sp,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				maxLines = 1,
				textAlign = TextAlign.Center
			)
		},
		trailingContent = {
			if (isSelected) {
				Waveform(isPlaying)
			}
		}
	)
}

@Composable
private fun Waveform(
	isPlaying: Boolean
) {
	val transition = rememberInfiniteTransition()
	Row(
		modifier = Modifier.height(18.dp),
		horizontalArrangement = Arrangement.spacedBy(2.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		repeat(5) { index ->
			val fraction by transition.animateFloat(
				initialValue = 0.2f,
				targetValue = 1f,
				animationSpec = infiniteRepeatable(
					animation = tween(
						durationMillis = 400 + (index * 150),
						easing = FastOutSlowInEasing
					),
					repeatMode = RepeatMode.Reverse
				)
			)
			Box(
				modifier = Modifier
					.width(3.dp)
					.fillMaxHeight(if (isPlaying) fraction else .2f)
					.background(MaterialTheme.colorScheme.onSurface, shape = CircleShape)
			)
		}
	}
}

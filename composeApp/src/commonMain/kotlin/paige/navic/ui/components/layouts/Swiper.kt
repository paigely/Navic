package paige.navic.ui.components.layouts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import paige.navic.data.models.Settings
import paige.navic.icons.Icons
import paige.navic.icons.filled.SkipNext
import paige.navic.icons.filled.SkipPrevious
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun Swiper(
	onSwipeLeft: () -> Unit,
	onSwipeRight: () -> Unit,
	modifier: Modifier = Modifier,
	swipeThreshold: Float = 200f,
	enableHaptics: Boolean = true,
	background: @Composable (BoxScope.() -> Unit)? = null,
	content: @Composable BoxScope.() -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val haptic = LocalHapticFeedback.current

	val offsetX = remember { Animatable(0f) }

	var isPastThreshold by remember { mutableStateOf(false) }

	fun handleSwipeRelease() {
		coroutineScope.launch {
			val targetValue = offsetX.value

			if (abs(targetValue) > swipeThreshold) {
				if (targetValue > 0) {
					onSwipeRight()
				} else {
					onSwipeLeft()
				}
				offsetX.animateTo(
					targetValue = if (targetValue > 0) targetValue + 50f else targetValue - 50f,
					animationSpec = spring(stiffness = Spring.StiffnessMedium)
				)
			}

			offsetX.animateTo(
				targetValue = 0f,
				animationSpec = spring(
					dampingRatio = Spring.DampingRatioLowBouncy,
					stiffness = Spring.StiffnessLow
				)
			)
		}
	}

	Box(
		modifier = modifier
			.fillMaxWidth(),
		contentAlignment = Alignment.Center
	) {
		val progress = (abs(offsetX.value) / swipeThreshold).coerceAtLeast(0f)

		background?.invoke(this@Box)

		Row(
			modifier = Modifier.matchParentSize().padding(horizontal = 16.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			SwipeIndicatorIcon(
				icon = Icons.Filled.SkipPrevious,
				isVisible = offsetX.value > 0,
				progress = progress
			)
			SwipeIndicatorIcon(
				icon = Icons.Filled.SkipNext,
				isVisible = offsetX.value < 0,
				progress = progress
			)
		}

		Box(
			modifier = Modifier
				.offset { IntOffset(offsetX.value.roundToInt(), 0) }
				.draggable(
					enabled = Settings.shared.swipeToSkip,
					orientation = Orientation.Horizontal,
					state = rememberDraggableState { delta ->
						coroutineScope.launch {
							offsetX.snapTo(offsetX.value + delta)
						}
					},
					onDragStopped = {
						handleSwipeRelease()
					}
				)
		) {
			content()
		}

		LaunchedEffect(offsetX.value) {
			val pastLimit = abs(offsetX.value) > swipeThreshold
			if (pastLimit && !isPastThreshold) {
				if (enableHaptics) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
			}
			isPastThreshold = pastLimit
		}
	}
}

@Composable
private fun SwipeIndicatorIcon(
	icon: ImageVector,
	isVisible: Boolean,
	progress: Float
) {
	Icon(
		imageVector = icon,
		contentDescription = null,
		tint = MaterialTheme.colorScheme.onSurface,
		modifier = Modifier
			.padding((progress * 10).dp)
			.size(32.dp)
			.alpha(if (isVisible) progress else 0f)
			.scale(if (isVisible) 0.5f + (progress * 0.5f) else 0.5f)
	)
}

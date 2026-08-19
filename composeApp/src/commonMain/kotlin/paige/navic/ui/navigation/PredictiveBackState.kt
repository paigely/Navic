package paige.navic.ui.navigation

import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.navigationevent.NavigationEvent.Companion.EDGE_RIGHT
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.NavigationEventTransitionState
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState

@Composable
fun rememberPredictiveBackState(
	onBackCompleted: () -> Unit
): PredictiveBackState {
	val state = rememberNavigationEventState(NavigationEventInfo.None)

	var animationActive by rememberSaveable { mutableStateOf(false) }
	var initialTouchX by rememberSaveable { mutableStateOf(0f) }
	var initialTouchY by rememberSaveable { mutableStateOf(0f) }
	var offsetX by rememberSaveable { mutableStateOf(0f) }
	var offsetY by rememberSaveable { mutableStateOf(0f) }
	var progress by rememberSaveable { mutableStateOf(0f) }
	val easedProgress = EaseOutExpo.transform(progress)

	val onBackCancelled = {
		initialTouchY = 0f
		offsetX = 0f
		offsetY = 0f
		progress = 0f
		animationActive = false
	}

	NavigationBackHandler(
		state = state,
		isBackEnabled = true,
		onBackCompleted = onBackCompleted,
		onBackCancelled = onBackCancelled
	)

	LaunchedEffect(state.transitionState) {
		when (val transitionState = state.transitionState) {
			is NavigationEventTransitionState.InProgress -> {
				if (!animationActive) {
					initialTouchX = transitionState.latestEvent.touchX
					initialTouchY = transitionState.latestEvent.touchY
				}
				offsetX = if (transitionState.latestEvent.swipeEdge == EDGE_RIGHT) {
					(transitionState.latestEvent.touchX - initialTouchX) / 22
				} else {
					transitionState.latestEvent.touchX / 22
				}
				offsetY = (transitionState.latestEvent.touchY - initialTouchY) / 20
				progress = transitionState.latestEvent.progress
				animationActive = true
			}

			else -> onBackCancelled()
		}
	}

	val animatedOffset by animateOffsetAsState(
		targetValue = Offset(x = offsetX, y = offsetY)
	)

	return PredictiveBackState(
		offsetX = animatedOffset.x * easedProgress,
		offsetY = animatedOffset.y,
		scale = 1 - (easedProgress / 8)
	)
}

data class PredictiveBackState(
	val offsetX: Float,
	val offsetY: Float,
	val scale: Float
)

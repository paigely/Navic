package paige.navic.ui.components.common

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

private class PullRefreshState(
	private val spec: AnimationSpec<Float>,
	private val anim: Animatable<Float, AnimationVector1D> =
		Animatable(0f)
) : PullToRefreshState {
	override val distanceFraction: Float
		get() = anim.value

	override val isAnimating: Boolean
		get() = anim.isRunning

	override suspend fun animateToThreshold() {
		anim.animateTo(
			targetValue = 1f,
			animationSpec = spec
		)
	}

	override suspend fun animateToHidden() {
		anim.animateTo(0f)
	}

	override suspend fun snapTo(targetValue: Float) {
		anim.snapTo(targetValue)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RefreshBox(
	modifier: Modifier = Modifier,
	isRefreshing: Boolean,
	onRefresh: () -> Unit = {},
	content: @Composable BoxScope.(topPadding: Dp) -> Unit
) {
	val spec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
	val state = remember { PullRefreshState(spec) }
	val scaleFraction = {
		if (isRefreshing) 1f
		else LinearOutSlowInEasing.transform(state.distanceFraction).coerceIn(0f, 1f)
	}
	val density = LocalDensity.current
	val offset by animateIntAsState(
		with(density) {
			if (isRefreshing)
				-128.dp.roundToPx()
			else -96.dp.roundToPx()
		}
	)
	PullToRefreshBox(
		isRefreshing = isRefreshing,
		onRefresh = onRefresh,
		state = state,
		modifier = modifier,
		indicator = {
			if (state.distanceFraction > 0.01f) {
				Popup(
					alignment = Alignment.TopCenter,
					offset = IntOffset(
						x = 0,
						y = offset
					),
					properties = PopupProperties(clippingEnabled = false)
				) {
					Box(
						Modifier
							.padding(vertical = 24.dp * (state.distanceFraction * 2f))
							.align(Alignment.TopCenter)
							.graphicsLayer {
								scaleX = scaleFraction()
								scaleY = scaleFraction()
								clip = false
							}
					) {
						PullToRefreshDefaults.LoadingIndicator(
							state = state,
							isRefreshing = isRefreshing
						)
					}
				}
			}
		}
	) {
		content(48.dp * (state.distanceFraction * 1.5f))
	}
}

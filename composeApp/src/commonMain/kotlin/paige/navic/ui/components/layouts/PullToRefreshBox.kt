package paige.navic.ui.components.layouts

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.pulltorefresh.PullToRefreshBox as M3PullToRefreshBox

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
	isRefreshing: Boolean,
	onRefresh: () -> Unit,
	modifier: Modifier = Modifier,
	state: PullToRefreshState = rememberPullToRefreshState(),
	content: @Composable BoxScope.() -> Unit
) {
	M3PullToRefreshBox(
		modifier = modifier,
		state = state,
		isRefreshing = isRefreshing,
		onRefresh = onRefresh,
		indicator = {
			Box(
				Modifier.align(Alignment.TopCenter).graphicsLayer {
					val scaleFraction = if (isRefreshing) 1f
					else LinearOutSlowInEasing.transform(state.distanceFraction).coerceIn(0f, 1f)
					scaleX = scaleFraction
					scaleY = scaleFraction
				}
			) {
				PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = isRefreshing)
			}
		},
		content = content
	)
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PullToRefreshBox(
	onRefresh: () -> Unit,
	finished: Boolean,
	modifier: Modifier = Modifier,
	key: Any? = finished,
	state: PullToRefreshState = rememberPullToRefreshState(),
	content: @Composable BoxScope.() -> Unit
) {
	var isRefreshing by remember { mutableStateOf(false) }

	LaunchedEffect(key) {
		if (finished) {
			isRefreshing = false
		}
	}

	PullToRefreshBox(
		isRefreshing = isRefreshing,
		onRefresh = {
			isRefreshing = true
			onRefresh()
		},
		modifier = modifier,
		state = state,
		content = content
	)
}

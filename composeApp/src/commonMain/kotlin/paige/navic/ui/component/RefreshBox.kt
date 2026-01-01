package paige.navic.ui.component

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RefreshBox(
	modifier: Modifier = Modifier,
	isRefreshing: Boolean,
	onRefresh: () -> Unit = {},
	content: @Composable BoxScope.() -> Unit
) {
	val state = rememberPullToRefreshState()
	val scaleFraction = {
		if (isRefreshing) 1f
		else LinearOutSlowInEasing.transform(state.distanceFraction).coerceIn(0f, 1f)
	}
	PullToRefreshBox(
		isRefreshing = isRefreshing,
		onRefresh = onRefresh,
		state = state,
		modifier = modifier,
		indicator = {
			Box(
				Modifier
					.align(Alignment.TopCenter)
					.graphicsLayer {
						scaleX = scaleFraction()
						scaleY = scaleFraction()
					}
			) {
				PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = isRefreshing)
			}
		}
	) {
		content()
	}
}

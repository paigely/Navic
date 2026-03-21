package paige.navic.ui.components.layouts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarCollapseMode
import paige.navic.data.models.settings.enums.PlayerBarStyle
import paige.navic.utils.easedVerticalGradient

@Composable
fun RootBottomBar(
	scrolled: Boolean,
	modifier: Modifier = Modifier,
	shadows: Boolean = true,
	hidePlayerBar: Boolean = false,
	bottomBarWindowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
) {
	val scrolled = scrolled && Settings.shared.bottomBarCollapseMode == BottomBarCollapseMode.OnScroll
	val progress by animateFloatAsState(
		targetValue = if (scrolled) 0f else 1f,
		animationSpec = spring(
			dampingRatio = Spring.DampingRatioLowBouncy,
			stiffness = Spring.StiffnessMediumLow
		)
	)
	val shadowFadeProgress by animateFloatAsState(
		targetValue = if (scrolled || !shadows) 0f else 1f,
		animationSpec = tween(durationMillis = 600)
	)
	Column(
		modifier = modifier.then(if (Settings.shared.playerBarStyle == PlayerBarStyle.Detached)
			Modifier.background(
				Brush.easedVerticalGradient(color = MaterialTheme.colorScheme.surface.copy(alpha = shadowFadeProgress))
			)
		else Modifier)
	) {
		if (!hidePlayerBar) PlayerBar(
			modifier = Modifier.graphicsLayer {
				alpha = progress.coerceIn(0f..1f)
				translationY = ((1f - progress) * (size.height * 2)).coerceAtLeast(
					if (Settings.shared.playerBarStyle == PlayerBarStyle.Detached) -2048f else 0f
				)
			},
			clicksEnabled = !scrolled
		)
		BottomBar(
			containerColor = if (Settings.shared.playerBarStyle == PlayerBarStyle.Detached)
				NavigationBarDefaults.containerColor.copy(alpha = 0f)
			else NavigationBarDefaults.containerColor,
			windowInsets = bottomBarWindowInsets,
			modifier = Modifier.graphicsLayer {
				alpha = progress.coerceIn(0f..1f)
				translationY =((1f - progress) * size.height).coerceAtLeast(
					if (Settings.shared.playerBarStyle == PlayerBarStyle.Detached) -2048f else 0f
				)
			},
			enabled = !scrolled
		)
	}
}

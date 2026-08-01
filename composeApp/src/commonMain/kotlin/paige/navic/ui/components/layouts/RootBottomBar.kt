package paige.navic.ui.components.layouts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.BottomBarCollapseMode
import paige.navic.domain.models.settings.MiniPlayerStyle

@Composable
fun RootBottomBar(
	scrolled: Boolean,
	modifier: Modifier = Modifier,
	shadows: Boolean = true,
	hideMiniPlayer: Boolean = false,
	bottomBarWindowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val scrolled =
		scrolled && preferenceManager.bottomBarCollapseMode == BottomBarCollapseMode.OnScroll
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
		modifier = modifier
			.fillMaxWidth()
			.background(
				if (preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached)
					Brush.verticalGradient(
						0f to Color.Transparent,
						0.4f to MaterialTheme.colorScheme.surface.copy(alpha = shadowFadeProgress * 0.7f),
						1f to MaterialTheme.colorScheme.surface.copy(alpha = shadowFadeProgress)
					)
				else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
			)
	) {
		if (!hideMiniPlayer) MiniPlayer(
			modifier = Modifier.graphicsLayer {
				alpha = progress.coerceIn(0f..1f)
				translationY = ((1f - progress) * (size.height * 2)).coerceAtLeast(
					if (preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached) -2048f else 0f
				)
			},
			enabled = !scrolled
		)
		BottomBar(
			containerColor = if (preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached)
				Color.Transparent
			else NavigationBarDefaults.containerColor,
			windowInsets = bottomBarWindowInsets,
			modifier = Modifier
				.fillMaxWidth()
				.graphicsLayer {
					alpha = progress.coerceIn(0f..1f)
					translationY = ((1f - progress) * size.height).coerceAtLeast(
						if (preferenceManager.miniPlayerStyle == MiniPlayerStyle.Detached) -2048f else 0f
					)
				},
			enabled = !scrolled
		)
	}
}

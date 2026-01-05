package paige.navic.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun Modifier.shimmerLoading(
	durationMillis: Int = 1100,
): Modifier {
	val transition = rememberInfiniteTransition(label = "")

	val translateAnimation by transition.animateFloat(
		initialValue = -200f,
		targetValue = 600f,
		animationSpec = infiniteRepeatable(
			animation = tween(
				durationMillis = durationMillis,
				easing = LinearEasing,
			),
			repeatMode = RepeatMode.Restart,
		),
		label = "",
	)

	return drawBehind {
		drawRect(
			brush = Brush.linearGradient(
				colors = listOf(
					Color.LightGray.copy(alpha = 0.1f),
					Color.LightGray.copy(alpha = 0.2f),
					Color.LightGray.copy(alpha = 0.1f),
				),
				start = Offset(x = translateAnimation, y = translateAnimation),
				end = Offset(x = translateAnimation + 200f, y = translateAnimation + 200f),
			)
		)
	}
}

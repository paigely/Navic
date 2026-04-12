package paige.navic.ui.components.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Waveform(
	modifier: Modifier = Modifier,
	isPlaying: Boolean
) {
	val transition = rememberInfiniteTransition()
	Row(
		modifier = modifier.height(18.dp),
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
			val limit by animateFloatAsState(
				if (isPlaying) 1f else .2f
			)
			Box(
				modifier = Modifier
					.width(3.dp)
					.fillMaxHeight(fraction.coerceAtMost(limit))
					.background(MaterialTheme.colorScheme.onSurface, shape = CircleShape)
			)
		}
	}
}

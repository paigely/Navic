package paige.navic.utils

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.delay
import paige.navic.ui.navigation.EmphasizedDecelerateEasing

@Composable
fun Modifier.fadeFromTop(): Modifier {
	var visible by rememberSaveable { mutableStateOf(true) }
	val progress by animateFloatAsState(
		targetValue = if (visible) 0f else 1f,
		animationSpec = tween(durationMillis = 900, easing = EmphasizedDecelerateEasing)
	)
	LaunchedEffect(Unit) {
		delay(25)
		visible = false
	}
	return this then Modifier
		.graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
		.drawWithContent {
			drawContent()
			drawRect(
				brush = Brush.verticalGradient(
					0f to Color.Black,
					progress to Color.Black,
					(progress + .25f).coerceAtMost(1f) to Color.Transparent,
					1f to Color.Transparent
				),
				blendMode = BlendMode.DstIn
			)
		}
}

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

@Composable
fun Modifier.onRightClick(
	callback: () -> Unit
): Modifier {
	return this.pointerInput(Unit) {
		awaitPointerEventScope {
			while (true) {
				val event = awaitPointerEvent()
				if (event.buttons.isSecondaryPressed) {
					callback()
				}
			}
		}
	}
}

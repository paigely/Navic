package paige.navic.ui.component.common

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import paige.navic.data.model.Settings

@Composable
fun MarqueeText(
	text: String,
	modifier: Modifier = Modifier
) {
	if (Settings.shared.useMarquee) {
		Marquee(modifier) {
			Text(text, maxLines = 1)
		}
	} else {
		Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Marquee(
	modifier: Modifier = Modifier,
	edgeWidth: Dp = 16.dp,
	animationSpec: AnimationSpec<Float> = tween(4000),
	delayMillis: Int = 1000,
	content: @Composable () -> Unit
) {
	val scrollState = rememberScrollState()
	val edgeWidthPx = with(LocalDensity.current) { edgeWidth.toPx() }

	LaunchedEffect(scrollState.maxValue) {
		if (scrollState.maxValue == 0) return@LaunchedEffect

		while (true) {
			delay(delayMillis.toLong())

			scrollState.animateScrollTo(
				value = scrollState.maxValue,
				animationSpec = animationSpec
			)

			delay(delayMillis.toLong())

			scrollState.animateScrollTo(
				value = 0,
				animationSpec = animationSpec
			)
		}
	}

	Box(
		modifier = modifier
			.graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
			.drawWithContent {
				drawContent()

				val startFadeAlpha = (scrollState.value / edgeWidthPx).coerceIn(0f, 1f)
				val endFadeAlpha = ((scrollState.maxValue - scrollState.value) / edgeWidthPx).coerceIn(0f, 1f)

				if (startFadeAlpha > 0f) {
					drawFadingEdge(
						isStart = true,
						width = edgeWidthPx,
						alpha = startFadeAlpha
					)
				}

				if (endFadeAlpha > 0f) {
					drawFadingEdge(
						isStart = false,
						width = edgeWidthPx,
						alpha = endFadeAlpha
					)
				}
			}
	) {
		Row(
			modifier = Modifier.horizontalScroll(scrollState, false)
		) {
			content()
		}
	}
}

private fun ContentDrawScope.drawFadingEdge(
	isStart: Boolean,
	width: Float,
	alpha: Float
) {
	val gradientColors = listOf(Color.Black, Color.Transparent)

	val startX = if (isStart) 0f else size.width - width
	val endX = if (isStart) width else size.width

	val startPoint = if (isStart) Offset(startX, 0f) else Offset(endX, 0f)
	val endPoint = if (isStart) Offset(endX, 0f) else Offset(startX, 0f)

	drawRect(
		brush = Brush.linearGradient(
			colors = gradientColors,
			start = startPoint,
			end = endPoint
		),
		topLeft = Offset(startX, 0f),
		size = Size(width, size.height),
		blendMode = BlendMode.DstOut,
		alpha = alpha
	)
}

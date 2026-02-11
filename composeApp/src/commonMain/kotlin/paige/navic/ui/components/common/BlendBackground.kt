package paige.navic.ui.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import kotlin.time.TimeSource

@Composable
fun BlendBackground(
	painter: Painter,
	modifier: Modifier = Modifier,
	isPaused: Boolean = false
) {
	var frameRotation by remember { mutableStateOf(0f) }
	var topLeftRotation by remember { mutableStateOf(0f) }
	var botRightRotation by remember { mutableStateOf(0f) }

	val colorMatrix = remember {
		ColorMatrix().apply { setToSaturation(1f) }
	}

	LaunchedEffect(isPaused) {
		if (!isPaused) {
			val timeSource = TimeSource.Monotonic
			var lastFrameMark = timeSource.markNow()

			while (true) {
				withFrameNanos { _ ->
					val now = timeSource.markNow()
					val elapsed = now - lastFrameMark
					val elapsedMillis = elapsed.toDouble(kotlin.time.DurationUnit.MILLISECONDS).toFloat()
					lastFrameMark = now

					frameRotation -= (360f / 24000f) * elapsedMillis
					topLeftRotation += (360f / 12000f) * elapsedMillis
					botRightRotation += (360f / 20000f) * elapsedMillis
				}
			}
		}
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
			.blur(80.dp)
	) {
		Image(
			painter = painter,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			colorFilter = ColorFilter.colorMatrix(colorMatrix),
			modifier = Modifier.fillMaxSize()
		)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.rotate(frameRotation)
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(0.5f)
					.fillMaxHeight(0.5f)
					.align(Alignment.TopStart)
			) {
				Image(
					painter = painter,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					alignment = Alignment.TopStart,
					colorFilter = ColorFilter.colorMatrix(colorMatrix),
					modifier = Modifier
						.fillMaxSize()
						.rotate(topLeftRotation)
				)
			}
			Box(
				modifier = Modifier
					.fillMaxWidth(0.5f)
					.fillMaxHeight(0.5f)
					.align(Alignment.BottomEnd)
			) {
				Image(
					painter = painter,
					contentDescription = null,
					contentScale = ContentScale.Crop,
					alignment = Alignment.BottomEnd,
					colorFilter = ColorFilter.colorMatrix(colorMatrix),
					modifier = Modifier
						.fillMaxSize()
						.rotate(botRightRotation)
				)
			}
		}
		Spacer(
			modifier = Modifier
				.fillMaxSize()
				.drawWithContent {
					drawContent()
					drawRect(color = Color.Black.copy(alpha = 0.4f))
				}
		)
	}
}
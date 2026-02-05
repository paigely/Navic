package paige.navic.ui.component.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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

@Composable
fun BlendBackground(
	painter: Painter,
	modifier: Modifier = Modifier,
	isPaused: Boolean = false
) {
	val infiniteTransition = rememberInfiniteTransition(label = "BlendAnimations")

	val frameRotation by infiniteTransition.animateFloat(
		initialValue = 0f, targetValue = -360f,
		animationSpec = infiniteRepeatable(
			animation = tween(24000, easing = LinearEasing)
		), label = "FrameRotation"
	)

	val topLeftRotation by infiniteTransition.animateFloat(
		initialValue = 0f, targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(12000, easing = LinearEasing)
		), label = "TLRotation"
	)

	val botRightRotation by infiniteTransition.animateFloat(
		initialValue = 0f, targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(20000, easing = LinearEasing)
		), label = "BRRotation"
	)

	val colorMatrix = remember {
		ColorMatrix().apply { setToSaturation(2.2f) }
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
				.rotate(if (isPaused) 0f else frameRotation)
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
						.rotate(if (isPaused) 0f else topLeftRotation)
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
						.rotate(if (isPaused) 0f else botRightRotation)
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
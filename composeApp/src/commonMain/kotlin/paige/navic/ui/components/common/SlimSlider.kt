package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule

@Composable
fun SlimSlider(
	value: Float,
	onValueChange: (Float) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true
) {
	var sliderWidth by remember { mutableFloatStateOf(0f) }

	Box(
		modifier = modifier
			.height(44.dp)
			.fillMaxWidth()
			.onGloballyPositioned { coordinates ->
				sliderWidth = coordinates.size.width.toFloat()
			}
			.pointerInput(sliderWidth) {
				detectTapGestures { offset ->
					if (sliderWidth > 0 && enabled) {
						val newValue = (offset.x / sliderWidth).coerceIn(0f, 1f)
						onValueChange(newValue)
					}
				}
			}
			.pointerInput(sliderWidth) {
				detectDragGestures { change, _ ->
					if (sliderWidth > 0 && enabled) {
						val newValue = (change.position.x / sliderWidth).coerceIn(0f, 1f)
						onValueChange(newValue)
					}
				}
			},
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.height(16.dp)
				.fillMaxWidth()
				.clip(ContinuousCapsule)
				.background(MaterialTheme.colorScheme.secondaryContainer)
		) {
			Box(
				modifier = Modifier
					.offset(x = (-6).dp)
					.size(4.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.primary)
					.align(Alignment.CenterEnd)
			)
			Box(
				modifier = Modifier
					.clip(ContinuousCapsule)
					.fillMaxWidth(value)
					.fillMaxHeight()
					.background(MaterialTheme.colorScheme.primary)
			)
		}
	}
}

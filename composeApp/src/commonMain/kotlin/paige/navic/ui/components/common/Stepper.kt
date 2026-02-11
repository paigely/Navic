package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle

@Composable
fun Stepper(
	value: Int,
	onValueChange: (Int) -> Unit,
	modifier: Modifier = Modifier,
	minValue: Int = Int.MIN_VALUE,
	maxValue: Int = Int.MAX_VALUE
) {
	Surface(
		modifier = modifier,
		shape = ContinuousRoundedRectangle(12.dp),
		tonalElevation = 2.dp,
		shadowElevation = 2.dp,
		color = MaterialTheme.colorScheme.surfaceContainerHighest
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier.height(40.dp)
		) {
			StepperButton(
				enabled = value > minValue,
				onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) },
				content = { Text("−", color = MaterialTheme.colorScheme.onSurface) }
			)
			VerticalDivider(
				modifier = Modifier
					.fillMaxHeight()
					.width(1.dp),
				color = MaterialTheme.colorScheme.surfaceContainer
			)
			StepperButton(
				enabled = value < maxValue,
				onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) },
				content = { Text("+", color = MaterialTheme.colorScheme.onSurface) }
			)
		}
	}
}

@Composable
private fun StepperButton(
	enabled: Boolean,
	onClick: () -> Unit,
	content: @Composable () -> Unit
) {
	TextButton(
		onClick = onClick,
		enabled = enabled,
		shape = RectangleShape,
		modifier = Modifier.fillMaxHeight().width(40.dp)
	) {
		ProvideTextStyle(MaterialTheme.typography.titleMedium) {
			content()
		}
	}
}

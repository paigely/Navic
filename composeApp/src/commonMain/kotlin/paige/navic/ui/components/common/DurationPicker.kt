package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun DurationPicker(
	duration: Duration,
	onDurationChange: (Duration) -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
) {
	val totalMinutes = duration.inWholeMinutes
	val hours = totalMinutes / 60
	val minutes = totalMinutes % 60

	Row(
		modifier = modifier,
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.Center
	) {
		TimeCard(
			value = hours,
			onValueChange = { newHours ->
				onDurationChange(newHours.hours + minutes.minutes)
			},
			enabled = enabled
		)

		Text(
			text = ":",
			style = MaterialTheme.typography.headlineLarge,
			color = MaterialTheme.colorScheme.onSurface,
			modifier = Modifier.padding(horizontal = 8.dp)
		)

		TimeCard(
			value = minutes,
			onValueChange = { newMinutes ->
				onDurationChange(hours.hours + newMinutes.minutes)
			},
			isMinutes = true,
			enabled = enabled
		)
	}
}

@Composable
private fun TimeCard(
	value: Long,
	onValueChange: (Long) -> Unit,
	isMinutes: Boolean = false,
	enabled: Boolean
) {
	var text by remember(value) { mutableStateOf(value.toString().padStart(2, '0')) }

	val focus = remember { FocusRequester() }
	var isFocused by remember { mutableStateOf(false) }

	val borderColor = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent
	val backgroundColor = if (isFocused) {
		MaterialTheme.colorScheme.primaryContainer
	} else {
		MaterialTheme.colorScheme.surfaceVariant
	}

	BasicTextField(
		value = text,
		onValueChange = { input ->
			val numericString = input.filter { it.isDigit() }

			val newText = if (numericString.length > 2) {
				numericString.takeLast(2)
			} else {
				numericString.padStart(2, '0')
			}

			val newValue = newText.toLongOrNull() ?: 0L

			if (isMinutes && newValue > 59) {
				return@BasicTextField
			}

			text = newText
			onValueChange(newValue)
		},
		enabled = enabled,
		textStyle = MaterialTheme.typography.headlineLarge.copy(
			color = if (isFocused) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
			textAlign = TextAlign.Center,
			fontSize = 50.sp
		),
		keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
		singleLine = true,
		cursorBrush = SolidColor(MaterialTheme.colorScheme.onPrimaryContainer),
		decorationBox = { innerTextField ->
			Box(
				modifier = Modifier
					.width(120.dp)
					.background(backgroundColor, ContinuousRoundedRectangle(12.dp))
					.border(2.dp, borderColor, ContinuousRoundedRectangle(11.dp))
					.padding(vertical = 8.dp),
				contentAlignment = Alignment.Center
			) {
				innerTextField()
			}
		},
		modifier = Modifier
			.focusRequester(focus)
			.onFocusChanged { isFocused = it.isFocused },
	)
}
package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FormButton(
	onClick: () -> Unit,
	color: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
	disabledColor: Color = color.copy(alpha = .5f),
	enabled: Boolean = true,
	content: @Composable () -> Unit
) {
	FormRow(
		onClick = if (enabled) onClick else null,
		horizontalArrangement = Arrangement.Center,
		contentPadding = PaddingValues(14.dp),
		rounding = 5.dp,
		color = if (enabled) color else disabledColor
	) {
		CompositionLocalProvider(
			LocalContentColor provides MaterialTheme.colorScheme
				.contentColorFor(color)
		) {
			content()
		}
	}
}

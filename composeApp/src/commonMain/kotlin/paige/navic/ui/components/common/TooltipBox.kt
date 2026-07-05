package paige.navic.ui.components.common

import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable

/**
 * wrapper for `androidx.compose.material3.TooltipBox` but simpler to use
 */
@Composable
fun TooltipBox(
	text: String,
	content: @Composable () -> Unit
) {
	androidx.compose.material3.TooltipBox(
		positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
			positioning = TooltipAnchorPosition.Above
		),
		tooltip = { PlainTooltip { Text(text) } },
		state = rememberTooltipState(),
		content = content
	)
}

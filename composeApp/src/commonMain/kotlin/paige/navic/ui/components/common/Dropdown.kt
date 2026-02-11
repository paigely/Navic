package paige.navic.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import paige.navic.LocalCtx
import paige.navic.ui.theme.defaultFont

@Composable
fun Dropdown(
	modifier: Modifier = Modifier,
	expanded: Boolean = true,
	offset: DpOffset = DpOffset(0.dp, 0.dp),
	onDismissRequest: () -> Unit,
	content: @Composable ColumnScope.() -> Unit
) {
	DropdownMenu(
		expanded = expanded,
		offset = offset,
		onDismissRequest = onDismissRequest,
		containerColor = Color.Transparent,
		shadowElevation = 0.dp,
		modifier = modifier.widthIn(200.dp)
	) {
        Form(
            rounding = 20.dp,
            spacing = 2.5.dp
        ) {
            content()
        }
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DropdownItem(
	modifier: Modifier = Modifier,
	containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
	onClick: () -> Unit = {},
	text: @Composable () -> Unit = {},
	leadingIcon: @Composable () -> Unit = {},
	enabled: Boolean = true,
	rounding: Dp = 4.dp
) {
	val ctx = LocalCtx.current
	val color by animateColorAsState(
		if (enabled) {
			MaterialTheme.colorScheme.onSurface.copy(alpha = .95f)
		} else {
			MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
		},
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)
	FormRow(
		color = containerColor,
		rounding = rounding,
		contentPadding = PaddingValues(0.dp)
	) {
		DropdownMenuItem(
			text = {
				CompositionLocalProvider(
					LocalTextStyle provides TextStyle(
						fontFamily = defaultFont(
							grade = 100,
							width = 104f
						),
						color = color
					)
				) {
					text()
				}
			},
			onClick = {
				ctx.clickSound()
				onClick()
			},
			modifier = modifier,
			leadingIcon = {
				CompositionLocalProvider(
					LocalContentColor provides color
				) {
					Box(Modifier.size(20.dp)) {
						leadingIcon()
					}
				}
			},
			contentPadding = PaddingValues(horizontal = 16.dp, vertical = 2.dp),
			enabled = enabled
		)
	}
}
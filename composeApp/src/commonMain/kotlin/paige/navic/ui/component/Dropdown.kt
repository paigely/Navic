package paige.navic.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.ui.theme.googleSans

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

@Composable
fun DropdownItem(
	modifier: Modifier = Modifier,
	containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
	text: StringResource,
	onClick: () -> Unit = {},
	leadingIcon: DrawableResource? = null,
	enabled: Boolean = true,
) {
	val ctx = LocalCtx.current
	val color by animateColorAsState(
		if (enabled) {
			MaterialTheme.colorScheme.onSurface.copy(alpha = .95f)
		} else {
			MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
		}
	)
	FormRow(
		color = containerColor,
		rounding = 4.dp,
		contentPadding = PaddingValues(2.dp)
	) {
		DropdownMenuItem(
			text = { Text(
				stringResource(text),
				fontFamily = googleSans(
					grade = 100,
					width = 104f
				),
				modifier = Modifier.padding(start = 2.dp),
				color = color
			) },
			onClick = {
				ctx.clickSound()
				onClick()
			},
			modifier = modifier,
			leadingIcon = {
				leadingIcon?.let {
					Icon(
						vectorResource(it),
						contentDescription = null,
						tint = color,
						modifier = Modifier.size(20.dp)
					)
				}
			},
			contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
			enabled = enabled
		)
	}
}
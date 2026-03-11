package paige.navic.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import paige.navic.LocalCtx
import paige.navic.data.models.Settings
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.ui.theme.defaultFont

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <Item> SelectionDropdown(
	items: List<Item>,
	label: @Composable (item: Item) -> String,
	selection: Item,
	onSelect: (item: Item) -> Unit,
	expanded: Boolean,
	onDismissRequest: () -> Unit
) {
	CompositionLocalProvider(
		LocalTextStyle provides TextStyle(
			fontFamily = defaultFont(100, round = 100f)
		),
		LocalMinimumInteractiveComponentSize provides 0.dp
	) {
		DropdownMenu(
			expanded = expanded,
			onDismissRequest = onDismissRequest,
			containerColor = Color.Transparent,
			shadowElevation = 0.dp
		) {
			Surface(
				modifier = Modifier
					.wrapContentSize()
					.heightIn(max = 600.dp)
					.padding(top = 4.dp)
					.padding(bottom = 8.dp)
					.padding(horizontal = 8.dp),
				color = if (Settings.shared.theme.isMaterialLike())
					MaterialTheme.colorScheme.surfaceContainerHigh
				else if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainer
				else MaterialTheme.colorScheme.surface,
				shape = MaterialTheme.shapes.large,
				shadowElevation = 3.dp
			) {
				Column(
					modifier = Modifier.verticalScroll(rememberScrollState())
				) {
					items.forEachIndexed { index, item ->
						SelectionDropdownItem(
							label = label(item),
							selected = selection == item,
							onClick = {
								onDismissRequest()
								onSelect(item)
							},
							index = index
						)
						if (items.last() != item && !Settings.shared.theme.isMaterialLike()) {
							HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainerHighest)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SelectionDropdownItem(
	label: String,
	selected: Boolean,
	index: Int,
	onClick: () -> Unit
) {
	val ctx = LocalCtx.current
	val color by animateColorAsState(
		if (selected && Settings.shared.theme.isMaterialLike())
			MaterialTheme.colorScheme.tertiary
		else Color.Transparent,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val elevation by animateDpAsState(
		if (selected && Settings.shared.theme.isMaterialLike()) 2.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val alpha by animateFloatAsState(
		if (selected) 1f else 0f,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val label = @Composable {
		Text(label, fontFamily = defaultFont(100, round = 100f))
	}
	val icon = @Composable {
		Icon(
			Icons.Outlined.Check,
			null,
			modifier = Modifier.padding(
				start = if (Settings.shared.theme.isMaterialLike()) 6.dp else 0.dp,
				end = if (!Settings.shared.theme.isMaterialLike()) 8.dp else 0.dp
			).size(20.dp).alpha(alpha)
		)
	}
	var visible by remember { mutableStateOf(false) }
	val verticalPadding by animateDpAsState(if (visible) 13.dp else 5.dp)
	val scaleY by animateFloatAsState(if (visible) 1f else 0.5f)
	LaunchedEffect(Unit) {
		delay(10L * index)
		visible = true
	}
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(if (Settings.shared.theme.isMaterialLike()) 6.dp else 0.dp),
		color = color,
		shape = if (Settings.shared.theme.isMaterialLike()) MaterialTheme.shapes.medium else RectangleShape,
		shadowElevation = elevation,
		onClick = {
			ctx.clickSound()
			onClick()
		}
	) {
		Row(
			modifier = Modifier.padding(
				horizontal = 13.dp,
				vertical = verticalPadding
			).scale(1f, scaleY),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			if (Settings.shared.theme.isMaterialLike()) {
				label()
				icon()
			} else {
				CompositionLocalProvider(
					LocalContentColor provides if (selected)
						MaterialTheme.colorScheme.primary
					else MaterialTheme.colorScheme.onSurface
				) {
					icon()
					label()
					Spacer(Modifier.weight(1f))
				}
			}
		}
	}
}

package paige.navic.ui.components.common

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.ui.theme.defaultFont

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <Item>SelectionDropdown(
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
		)
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
				color = MaterialTheme.colorScheme.surfaceContainerHigh,
				shape = MaterialTheme.shapes.large,
				shadowElevation = 3.dp
			) {
				Column(
					modifier = Modifier.verticalScroll(rememberScrollState())
				) {
					items.forEach { item ->
						SelectionDropdownItem(
							label = label(item),
							selected = selection == item,
							onClick = { onSelect(item) }
						)
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
	onClick: () -> Unit
) {
	val color by animateColorAsState(
		if (selected)
			MaterialTheme.colorScheme.tertiary
		else Color.Transparent,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val elevation by animateDpAsState(
		if (selected) 2.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val alpha by animateFloatAsState(
		if (selected) 1f else 0f,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	Surface(
		modifier = Modifier
			.fillMaxWidth()
			.padding(6.dp),
		color = color,
		shape = MaterialTheme.shapes.medium,
		shadowElevation = elevation,
		onClick = { if (!selected) onClick() }
	) {
		Row(
			modifier = Modifier.padding(13.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(label, fontFamily = defaultFont(100, round = 100f))
			Icon(
				Icons.Outlined.Check,
				null,
				modifier = Modifier.padding(start = 6.dp).size(20.dp).alpha(alpha)
			)
		}
	}
}

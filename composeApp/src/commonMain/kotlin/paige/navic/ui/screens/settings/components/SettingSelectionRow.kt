package paige.navic.ui.screens.settings.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.SelectionDropdown

@Composable
fun <Item> SettingSelectionRow(
	title: @Composable () -> Unit,
	items: ImmutableList<Item>,
	label: @Composable (item: Item) -> String,
	description: String? = null,
	selection: Item,
	onSelect: (item: Item) -> Unit,
	footer: (@Composable () -> Unit)? = null
) {
	var expanded by rememberSaveable { mutableStateOf(false) }
	FormRow(
		onClick = { expanded = true },
		contentPadding = PaddingValues(14.dp)
	) {
		Column(Modifier.weight(1f)) {
			title()
			Box {
				CompositionLocalProvider(
					LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
				) {
					Text(buildString {
						append(label(selection))
						description?.let { description ->
							append(" // $description")
						}
					})
				}
				SelectionDropdown(
					items = items,
					label = label,
					selection = selection,
					onSelect = onSelect,
					expanded = expanded,
					footer = footer,
					onDismissRequest = { expanded = false },
				)
			}
		}
	}
}
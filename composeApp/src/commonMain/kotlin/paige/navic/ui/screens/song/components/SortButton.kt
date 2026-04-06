package paige.navic.ui.screens.song.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainSongListType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Sort
import paige.navic.ui.components.common.SelectionDropdown
import paige.navic.ui.components.layouts.TopBarButton

@Composable
fun SongListScreenSortButton(
	nested: Boolean,
	currentListType: DomainSongListType,
	onSetListType: (listType: DomainSongListType) -> Unit
) {
	val items = remember { DomainSongListType.entries.toImmutableList() }
	Box {
		var expanded by remember { mutableStateOf(false) }
		if (!nested) {
			IconButton(onClick = {
				expanded = true
			}) {
				Icon(
					Icons.Outlined.Sort,
					contentDescription = null
				)
			}
		} else {
			TopBarButton({
				expanded = true
			}) {
				Icon(
					Icons.Outlined.Sort,
					contentDescription = null
				)
			}
		}
		SelectionDropdown(
			items = items,
			label = { stringResource(it.displayName) },
			expanded = expanded,
			onDismissRequest = { expanded = false },
			selection = currentListType,
			onSelect = onSetListType
		)
	}
}

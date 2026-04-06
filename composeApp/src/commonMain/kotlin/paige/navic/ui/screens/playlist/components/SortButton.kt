package paige.navic.ui.screens.playlist.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_sort_ascending
import navic.composeapp.generated.resources.option_sort_descending
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.PlaylistSortMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Sort
import paige.navic.ui.components.common.SelectionDropdown
import paige.navic.ui.components.common.SelectionDropdownItem
import paige.navic.ui.components.layouts.TopBarButton

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistListScreenSortButton(
	nested: Boolean,
	onSortPlaylists: () -> Unit
) {
	val items = remember { PlaylistSortMode.entries.toImmutableList() }
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
			selection = Settings.shared.playlistSortMode,
			onSelect = {
				Settings.shared.playlistSortMode = it
				onSortPlaylists()
			},
			footer = {
				SelectionDropdownItem(
					label = stringResource(Res.string.option_sort_ascending),
					selected = !Settings.shared.playlistsReversed,
					onClick = {
						Settings.shared.playlistsReversed = false
						expanded = false
						onSortPlaylists()
					}
				)
				SelectionDropdownItem(
					label = stringResource(Res.string.option_sort_descending),
					selected = Settings.shared.playlistsReversed,
					onClick = {
						Settings.shared.playlistsReversed = true
						expanded = false
						onSortPlaylists()
					}
				)
			}
		)
	}
}

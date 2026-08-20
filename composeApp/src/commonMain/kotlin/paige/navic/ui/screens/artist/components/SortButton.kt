package paige.navic.ui.screens.artist.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.models.DomainFilter
import paige.navic.domain.models.settings.ListViewMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Sort
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.sheets.SortSheet

@Composable
fun ArtistListScreenSortButton(
	nested: Boolean,
	selectedSorting: DomainArtistListType,
	onSetSorting: (DomainArtistListType) -> Unit,
	selectedViewMode: ListViewMode,
	onSetViewMode: (ListViewMode) -> Unit,
	selectedFilters: Set<DomainFilter>,
	onToggleFilter: (DomainFilter) -> Unit
) {
	val entries = remember {
		persistentListOf(
			DomainArtistListType.AlphabeticalByName,
			DomainArtistListType.Random
		)
	}
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
		TopBarButton({ expanded = true }) {
			Icon(
				Icons.Outlined.Sort,
				contentDescription = null
			)
		}
	}
	if (expanded) {
		SortSheet(
			entries = entries,
			selectedSorting = selectedSorting,
			selectedReversed = false,
			onSetReversed = {},
			label = { stringResource(it.displayName) },
			onSetSorting = onSetSorting,
			onDismissRequest = { expanded = false },
			selectedViewMode = selectedViewMode,
			onSetViewMode = onSetViewMode,
			selectedFilters = selectedFilters,
			onToggleFilter = onToggleFilter
		)
	}
}

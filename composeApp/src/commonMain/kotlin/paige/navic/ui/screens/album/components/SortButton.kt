package paige.navic.ui.screens.album.components

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Sort
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.sheets.SortSheet
import paige.navic.ui.screens.library.components.label

@Composable
fun AlbumListScreenSortButton(
	nested: Boolean,
	selectedSorting: DomainAlbumListType,
	onSetSorting: (DomainAlbumListType) -> Unit,
	selectedReversed: Boolean,
	onSetReversed: (Boolean) -> Unit
) {
	val entries = remember {
		persistentListOf(
			DomainAlbumListType.AlphabeticalByArtist,
			DomainAlbumListType.Frequent,
			DomainAlbumListType.Recent,
			DomainAlbumListType.Newest,
			DomainAlbumListType.Starred,
			DomainAlbumListType.Random,
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
		TopBarButton({
			expanded = true
		}) {
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
			selectedReversed = selectedReversed,
			label = { it.label() },
			onSetSorting = onSetSorting,
			onSetReversed = onSetReversed,
			onDismissRequest = { expanded = false }
		)
	}
}

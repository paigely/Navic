package paige.navic.ui.screens.album.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_sort_alphabetical_by_artist
import navic.composeapp.generated.resources.option_sort_alphabetical_by_name
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Sort
import paige.navic.ui.components.common.SelectionDropdown
import paige.navic.ui.components.layouts.TopBarButton

@Composable
fun AlbumListScreenSortButton(
	nested: Boolean,
	currentListType: DomainAlbumListType,
	onSetListType: (listType: DomainAlbumListType) -> Unit
) {
	val items = remember {
		persistentListOf(
			DomainAlbumListType.Random,
			DomainAlbumListType.Newest,
			DomainAlbumListType.Frequent,
			DomainAlbumListType.Recent,
			DomainAlbumListType.Starred,
			DomainAlbumListType.AlphabeticalByArtist,
		)
	}
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
			label = {
				it.label()
			},
			expanded = expanded,
			onDismissRequest = { expanded = false },
			selection = currentListType,
			onSelect = onSetListType
		)
	}
}

@Composable
private fun DomainAlbumListType.label() =
	when (this) {
		DomainAlbumListType.Random -> stringResource(Res.string.option_sort_random)
		DomainAlbumListType.Newest -> stringResource(Res.string.option_sort_newest)
		DomainAlbumListType.Frequent -> stringResource(Res.string.option_sort_frequent)
		DomainAlbumListType.Recent -> stringResource(Res.string.option_sort_recent)
		DomainAlbumListType.AlphabeticalByName -> stringResource(Res.string.option_sort_alphabetical_by_name)
		DomainAlbumListType.AlphabeticalByArtist -> stringResource(Res.string.option_sort_alphabetical_by_artist)
		DomainAlbumListType.Starred -> stringResource(Res.string.option_sort_starred)
		else -> "$this"
	}

package paige.navic.ui.screens.library.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.screens.album.components.albumListScreenContent
import paige.navic.ui.screens.library.components.LibraryFilterChips
import paige.navic.ui.screens.library.components.label
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenAlbumsTab(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	albumsState: UiState<ImmutableList<DomainAlbum>>,
	selectedAlbum: DomainAlbum?,
	selectedAlbumIsStarred: Boolean,
	albumsListType: DomainAlbumListType,
	onSelectAlbum: (DomainAlbum) -> Unit,
	onClearAlbumSelection: () -> Unit,
	onStarSelectedAlbum: (Boolean) -> Unit,
	onSetListType: (DomainAlbumListType) -> Unit,
	onSetShareId: (String) -> Unit
) {
	val filterItems = remember {
		persistentListOf(
			DomainAlbumListType.Recent,
			DomainAlbumListType.Newest,
			DomainAlbumListType.Starred,
			DomainAlbumListType.Frequent,
			DomainAlbumListType.Random,
			DomainAlbumListType.AlphabeticalByArtist,
		)
	}

	Column(
		modifier = Modifier
			.nestedScroll(scrollBehavior.nestedScrollConnection)
			.fillMaxSize()
	) {
		LibraryFilterChips(
			items = filterItems,
			selectedItem = albumsListType,
			onItemSelect = onSetListType,
			label = { Text(it.label(), overflow = TextOverflow.Clip, maxLines = 1) },
			modifier = Modifier.padding(top = 8.dp)
		)

		ArtGrid(
			modifier = Modifier.weight(1f),
			contentPadding = innerPadding.withoutTop()
		) {
			albumListScreenContent(
				state = albumsState,
				starred = selectedAlbumIsStarred,
				selectedAlbum = selectedAlbum,
				onUpdateSelection = onSelectAlbum,
				onClearSelection = onClearAlbumSelection,
				onSetShareId = onSetShareId,
				onSetStarred = onStarSelectedAlbum
			)
		}
	}
}

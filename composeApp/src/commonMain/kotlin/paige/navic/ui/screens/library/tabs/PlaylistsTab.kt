package paige.navic.ui.screens.library.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.screens.library.components.LibraryFilterChips
import paige.navic.ui.screens.playlist.components.playlistListScreenContent
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenPlaylistsTab(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	playlistsState: UiState<ImmutableList<DomainPlaylist>>,
	selectedPlaylist: DomainPlaylist?,
	playlistsListType: DomainPlaylistListType,
	gridState: LazyGridState,
	onSelectPlaylist: (DomainPlaylist) -> Unit,
	onClearPlaylistSelection: () -> Unit,
	onSetListType: (DomainPlaylistListType) -> Unit,
	onSetShareId: (String) -> Unit,
	onSetPlaylistDeletionId: (String) -> Unit,
) {
	val filters = remember { DomainPlaylistListType.entries.toPersistentList() }

	Column(
		modifier = Modifier
			.nestedScroll(scrollBehavior.nestedScrollConnection)
			.fillMaxSize()
	) {
		LibraryFilterChips(
			items = filters,
			selectedItem = playlistsListType,
			onItemSelect = onSetListType,
			label = {
				Text(stringResource(it.displayName), overflow = TextOverflow.Clip, maxLines = 1)
			},
			modifier = Modifier.padding(top = 8.dp)
		)

		ArtGrid(
			modifier = Modifier.weight(1f),
			state = gridState,
			contentPadding = innerPadding.withoutTop()
		) {
			playlistListScreenContent(
				state = playlistsState,
				selectedPlaylist = selectedPlaylist,
				onUpdateSelection = onSelectPlaylist,
				onClearSelection = onClearPlaylistSelection,
				onSetShareId = onSetShareId,
				onSetDeletionId = onSetPlaylistDeletionId
			)
		}
	}
}

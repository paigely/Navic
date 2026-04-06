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
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import paige.navic.ui.screens.artist.components.ArtistListScreenContent
import paige.navic.ui.screens.library.components.LibraryFilterChips
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenArtistsTab(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	artistsState: UiState<ImmutableList<DomainArtist>>,
	selectedArtist: DomainArtist?,
	selectedArtistIsStarred: Boolean,
	artistsListType: DomainArtistListType,
	gridState: LazyGridState,
	onSelectArtist: (DomainArtist) -> Unit,
	onClearArtistSelection: () -> Unit,
	onStarSelectedArtist: (Boolean) -> Unit,
	onSetListType: (DomainArtistListType) -> Unit,
) {
	val filters = remember { DomainArtistListType.entries.toPersistentList() }

	Column(
		modifier = Modifier
			.nestedScroll(scrollBehavior.nestedScrollConnection)
			.fillMaxSize()
	) {
		LibraryFilterChips(
			items = filters,
			selectedItem = artistsListType,
			onItemSelect = onSetListType,
			label = {
				Text(
					text = stringResource(it.displayName),
					overflow = TextOverflow.Clip,
					maxLines = 1
				)
			},
			modifier = Modifier.padding(top = 8.dp)
		)

		ArtistListScreenContent(
			state = artistsState,
			starred = selectedArtistIsStarred,
			gridState = gridState,
			scrollBehavior = scrollBehavior,
			innerPadding = innerPadding,
			nested = true,
			selectedArtist = selectedArtist,
			onUpdateSelection = onSelectArtist,
			onClearSelection = onClearArtistSelection,
			onSetStarred = onStarSelectedArtist
		)
	}
}

package paige.navic.ui.screens.artist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_artists
import navic.composeapp.generated.resources.info_no_artists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.settings.ListViewMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Artist
import paige.navic.ui.components.common.AlphabeticalScroller
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.core.UiState
import paige.navic.ui.screens.artist.ArtistListScreenGridItem
import paige.navic.util.ui.withoutTop

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ArtistListScreenContent(
	state: UiState<ImmutableList<DomainArtist>>,
	starred: Boolean,
	gridState: LazyGridState,
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	nested: Boolean,
	selectedArtist: DomainArtist?,
	selectedArtistAlbums: ImmutableList<DomainAlbum>?,
	selectedViewMode: ListViewMode,
	onUpdateSelection: (DomainArtist) -> Unit,
	onClearSelection: () -> Unit,
	onSetStarred: (Boolean) -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
) {

	val data = state.data.orEmpty()

	val totalArtistCount = data.size

	val grouped = data.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
		.toList()
		.sortedBy { it.first }

	val headerIndices = remember(grouped) {
		var currentIndex = 1
		grouped.map { (letter, artists) ->
			val pos = currentIndex
			currentIndex += artists.size + 1
			letter.toString() to pos
		}.toImmutableList()
	}

	val textPadding = PaddingValues(
		horizontal = if (selectedViewMode == ListViewMode.List) 16.dp else 0.dp,
		vertical = 8.dp
	)

	Box {
		ArtGrid(
			modifier = if (!nested)
				Modifier.fillMaxSize()
					.nestedScroll(scrollBehavior.nestedScrollConnection)
			else Modifier.fillMaxSize(),
			state = gridState,
			contentPadding = innerPadding.withoutTop(),
			verticalArrangement = if (grouped.isEmpty()) {
				Arrangement.Center
			} else if (selectedViewMode == ListViewMode.List) {
				Arrangement.spacedBy(0.dp)
			} else {
				Arrangement.spacedBy(12.dp)
			},
			selectedViewMode = selectedViewMode
		) {
			if (totalArtistCount != 0) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					Row(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surface)
							.padding(textPadding.withoutTop()),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							pluralStringResource(
								Res.plurals.count_artists,
								totalArtistCount,
								totalArtistCount
							),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
			grouped.forEach { (letter, artists) ->
				stickyHeader {
					Row(
						modifier = Modifier
							.background(MaterialTheme.colorScheme.surface)
							.padding(textPadding),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = letter.toString(),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
				items(artists, { it.id }) { artist ->
					if (selectedViewMode == ListViewMode.Grid) {
						ArtistListScreenGridItem(
							modifier = Modifier.animateItem(),
							tab = "artists",
							artist = artist,
							selected = artist == selectedArtist,
							selectedArtistAlbums = selectedArtistAlbums,
							starred = starred,
							onSelect = { onUpdateSelection(artist) },
							onDeselect = { onClearSelection() },
							onSetStarred = { onSetStarred(it) },
							onPlayNext = onPlayNext,
							onAddToQueue = onAddToQueue
						)
					} else {
						ArtistListScreenListItem(
							modifier = Modifier.animateItem(),
							artist = artist,
							selected = artist == selectedArtist,
							selectedArtistAlbums = selectedArtistAlbums,
							starred = starred,
							onSelect = { onUpdateSelection(artist) },
							onDeselect = { onClearSelection() },
							onSetStarred = { onSetStarred(it) },
							onPlayNext = onPlayNext,
							onAddToQueue = onAddToQueue
						)
					}
				}
			}

			if (grouped.isEmpty()) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						icon = Icons.Outlined.Artist,
						label = stringResource(Res.string.info_no_artists)
					)
				}
			}
		}
		AlphabeticalScroller(
			state = gridState,
			headers = headerIndices,
			modifier = Modifier.align(Alignment.TopEnd)
		)
	}
}

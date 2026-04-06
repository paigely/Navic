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
import paige.navic.domain.models.DomainArtist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Artist
import paige.navic.ui.components.common.AlphabeticalScroller
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.screens.artist.ArtistsScreenItem
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

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
	onUpdateSelection: (DomainArtist) -> Unit,
	onClearSelection: () -> Unit,
	onSetStarred: (Boolean) -> Unit
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

	Box {
		ArtGrid(
			modifier = if (!nested)
				Modifier.fillMaxSize()
					.nestedScroll(scrollBehavior.nestedScrollConnection)
			else Modifier.fillMaxSize(),
			state = gridState,
			contentPadding = innerPadding.withoutTop(),
			verticalArrangement = if (grouped.isEmpty())
				Arrangement.Center
			else Arrangement.spacedBy(12.dp)
		) {
			item(span = { GridItemSpan(maxLineSpan) }) {
				Row(
					Modifier
						.background(MaterialTheme.colorScheme.surface)
						.padding(bottom = 8.dp),
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
			grouped.forEach { (letter, artists) ->
				item(span = { GridItemSpan(maxLineSpan) }) {
					Row(
						Modifier
							.background(MaterialTheme.colorScheme.surface)
							.padding(bottom = 8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						Text(
							text = letter.toString(),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
				items(artists, { it.id }) { artist ->
					ArtistsScreenItem(
						modifier = Modifier.animateItem(),
						tab = "artists",
						artist = artist,
						selected = artist == selectedArtist,
						starred = starred,
						onSelect = { onUpdateSelection(artist) },
						onDeselect = { onClearSelection() },
						onSetStarred = { onSetStarred(it) }
					)
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
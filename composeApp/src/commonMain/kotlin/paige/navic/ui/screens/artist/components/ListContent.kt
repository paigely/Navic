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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_artists
import navic.composeapp.generated.resources.info_no_artists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainArtist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Artist
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.screens.artist.ArtistsScreenItem
import paige.navic.utils.withoutTop

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ArtistListScreenContent(
	artists: LazyPagingItems<DomainArtist>,
	totalCount: Int,
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
	Box {
		ArtGrid(
			modifier = if (!nested)
				Modifier.fillMaxSize()
					.nestedScroll(scrollBehavior.nestedScrollConnection)
			else Modifier.fillMaxSize(),
			state = gridState,
			contentPadding = innerPadding.withoutTop(),
			verticalArrangement = if (artists.itemCount == 0)
				Arrangement.Center
			else Arrangement.spacedBy(12.dp)
		) {
			if (artists.itemCount > 0) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					Row(
						Modifier
							.background(MaterialTheme.colorScheme.surface)
							.padding(bottom = 8.dp),
						verticalAlignment = Alignment.CenterVertically
					) {
						// Note to myself: Total count is harder to get exactly from Paging items
						// but usually available in the LoadState or can be estimated.
						Text(
							pluralStringResource(
								Res.plurals.count_artists,
								totalCount,
								totalCount
							),
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}

			items(count = artists.itemCount, key = { index -> artists[index]?.id ?: index }) { index ->
				val artist = artists[index]
				if (artist != null) {
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

			if (artists.itemCount == 0 && artists.loadState.refresh is LoadState.NotLoading) {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						icon = Icons.Outlined.Artist,
						label = stringResource(Res.string.info_no_artists)
					)
				}
			}
		}
	}
}

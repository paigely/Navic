package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.count_albums
import navic.composeapp.generated.resources.count_artists
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.common.RefreshBox
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.ArtGridItem
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.component.layout.RootTopBar
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.viewmodel.ArtistsViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Artist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
	nested: Boolean = false,
	viewModel: ArtistsViewModel = viewModel { ArtistsViewModel() }
) {
	val artistsState by viewModel.artistsState.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar({ Text(stringResource(Res.string.title_artists)) }, scrollBehavior)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_artists)) })
			}
		}
	) { innerPadding ->
		RefreshBox(
			modifier = Modifier.padding(innerPadding).background(MaterialTheme.colorScheme.surface),
			isRefreshing = artistsState is UiState.Loading,
			onRefresh = { viewModel.refreshArtists() }
		) { topPadding ->
			AnimatedContent(artistsState, Modifier.padding(top = topPadding)) {
				when (it) {
					is UiState.Loading -> ArtGrid(Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
						artGridPlaceholder()
					}

					is UiState.Success -> {
						val grouped = it.data.flatMap { section ->
							section.artist.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
								.toList()
								.sortedBy { it.first }
						}

						LazyVerticalGrid(
							modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
							columns = GridCells.Adaptive(150.dp),
							contentPadding = PaddingValues(
								start = 16.dp,
								top = 16.dp,
								end = 16.dp,
								bottom = 200.dp,
							),
							verticalArrangement = Arrangement.spacedBy(12.dp),
							horizontalArrangement = Arrangement.spacedBy(12.dp),
						) {
							stickyHeader { _ ->
								Row(
									Modifier
										.background(MaterialTheme.colorScheme.surface)
										.padding(bottom = 8.dp),
									verticalAlignment = Alignment.CenterVertically
								) {
									Text(
										pluralStringResource(
											Res.plurals.count_artists,
											it.data.count(),
											it.data.count()
										),
										color = MaterialTheme.colorScheme.onSurfaceVariant
									)
								}
							}
							grouped.forEach { (letter, artists) ->
								stickyHeader {
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
								artistsScreenItems(artists, viewModel)
							}
						}
					}

					is UiState.Error -> ErrorBox(it)
				}
			}
		}
	}
}

@Composable
fun ArtistsScreenItem(
	modifier: Modifier = Modifier,
	artist: Artist,
	viewModel: ArtistsViewModel
) {
	val selection by viewModel.selectedArtist.collectAsState()
	val starredState by viewModel.starredState.collectAsState()
	Box(modifier) {
		ArtGridItem(
			imageModifier = Modifier.combinedClickable(
				onClick = {},
				onLongClick = { viewModel.selectArtist(artist) }
			),
			imageUrl = artist.coverArt,
			title = artist.name,
			subtitle = pluralStringResource(
				Res.plurals.count_albums,
				artist.albumCount ?: 0,
				artist.albumCount ?: 0
			) + "\n"
		)
		Dropdown(
			expanded = selection == artist,
			onDismissRequest = { viewModel.clearSelection() })
		{
			val starred = (starredState as? UiState.Success)?.data
			DropdownItem(
				text = if (starred == true)
					Res.string.action_remove_star
				else Res.string.action_star,
				leadingIcon = Res.drawable.unstar,
				onClick = {
					if (starred == true)
						viewModel.unstarSelectedArtist()
					else viewModel.starSelectedArtist()
					viewModel.clearSelection()
				},
				enabled = starred != null
			)
		}
	}
}

fun LazyGridScope.artistsScreenItems(
	data: List<Artist>,
	viewModel: ArtistsViewModel
) {
	items(data, { it.id }) { album ->
		ArtistsScreenItem(Modifier.animateItem(), album, viewModel)
	}
}
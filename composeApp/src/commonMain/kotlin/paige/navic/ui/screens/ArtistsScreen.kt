package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.ui.components.common.AlphabeticalScroller
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.RefreshBox
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.viewmodels.ArtistsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.onRightClick
import paige.subsonic.api.models.Artist

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistsScreen(
	nested: Boolean = false,
	viewModel: ArtistsViewModel = viewModel { ArtistsViewModel() }
) {
	val artistsState by viewModel.artistsState.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val gridState = rememberLazyGridState()

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
						val totalArtistCount = it.data.sumOf { section -> section.artist.size }

						val grouped = it.data.flatMap { section ->
							section.artist.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
								.toList()
								.sortedBy { it.first }
						}
						val headerIndices = remember(grouped) {
							var currentIndex = 1
							grouped.map { (letter, artists) ->
								val pos = currentIndex
								currentIndex += artists.size + 1
								letter.toString() to pos
							}
						}

						Row {
							ArtGrid(
								modifier = if (!nested)
									Modifier.weight(1f).nestedScroll(scrollBehavior.nestedScrollConnection)
								else Modifier.weight(1f),
								state = gridState
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
												totalArtistCount,
												totalArtistCount
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
							AlphabeticalScroller(
								state = gridState,
								headers = headerIndices
							)
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
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedArtist.collectAsState()
	val starredState by viewModel.starredState.collectAsState()
	Box(modifier) {
		ArtGridItem(
			imageModifier = Modifier
				.combinedClickable(
					onClick = {
						ctx.clickSound()
						backStack.add(Screen.Artist(artist.id))
					},
					onLongClick = { viewModel.selectArtist(artist) }
				)
				.onRightClick {
					viewModel.selectArtist(artist)
				},
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
				text = {
					Text(
						stringResource(
							if (starred == true)
								Res.string.action_remove_star
							else Res.string.action_star
						)
					)
				},
				leadingIcon = { Icon(Icons.Filled.Star, null) },
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

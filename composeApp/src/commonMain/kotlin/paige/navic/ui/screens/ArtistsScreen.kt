package paige.navic.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Artist
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.count_albums
import navic.composeapp.generated.resources.count_artists
import navic.composeapp.generated.resources.info_no_artists
import navic.composeapp.generated.resources.title_artists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.AlphabeticalScroller
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.viewmodels.ArtistsViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

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
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (!nested || Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = artistsState is UiState.Loading,
			onRefresh = { viewModel.refreshArtists() }
		) {
			Crossfade(artistsState) {
				when (it) {
					is UiState.Loading -> ArtGrid(
						Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
						contentPadding = innerPadding.withoutTop()
					) {
						artGridPlaceholder()
					}

					is UiState.Success -> {
						val totalArtistCount = it.data.size

						val grouped = it.data.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
							.toList()
							.sortedBy { it.first }

						val headerIndices = remember(grouped) {
							var currentIndex = 1
							grouped.map { (letter, artists) ->
								val pos = currentIndex
								currentIndex += artists.size + 1
								letter.toString() to pos
							}
						}

						Box {
							ArtGrid(
								modifier = if (!nested)
									Modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection)
								else Modifier.fillMaxSize(),
								state = viewModel.gridState,
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
									artistsScreenItems(artists, viewModel, "artists")
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
								state = viewModel.gridState,
								headers = headerIndices,
								modifier = Modifier.align(Alignment.TopEnd)
							)
						}
					}

					is UiState.Error -> ErrorBox(it, padding = innerPadding.withoutTop())
				}
			}
		}
	}
}

@Composable
fun ArtistsScreenItem(
	modifier: Modifier = Modifier,
	artist: Artist,
	tab: String,
	viewModel: ArtistsViewModel
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedArtist.collectAsState()
	val starredState by viewModel.starredState.collectAsState()
	Box(modifier) {
		ArtGridItem(
			onClick = {
				ctx.clickSound()
				backStack.add(Screen.Artist(artist.id))
			},
			onLongClick = { viewModel.selectArtist(artist) },
			coverArtId = artist.coverArtId,
			title = artist.name,
			subtitle = pluralStringResource(
				Res.plurals.count_albums,
				artist.albumCount,
				artist.albumCount
			),
			id = artist.id,
			tab = tab
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
				leadingIcon = {
					Icon(if (starred == true) Icons.Filled.Star else Icons.Outlined.Star, null)
				},
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
	viewModel: ArtistsViewModel,
	tab: String
) {
	items(data, { it.id }) { album ->
		ArtistsScreenItem(Modifier.animateItem(), album, tab, viewModel)
	}
}
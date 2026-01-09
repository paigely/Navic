package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.count_albums
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.ui.component.ArtGridItem
import paige.navic.ui.component.ArtGridPlaceholder
import paige.navic.ui.component.Dropdown
import paige.navic.ui.component.DropdownItem
import paige.navic.ui.component.ErrorBox
import paige.navic.ui.component.RefreshBox
import paige.navic.ui.viewmodel.ArtistsViewModel
import paige.navic.util.UiState

@Composable
fun ArtistsScreen(
	viewModel: ArtistsViewModel = viewModel { ArtistsViewModel() }
) {
	LocalCtx.current
	LocalNavStack.current
	LocalHapticFeedback.current

	val artistsState by viewModel.artistsState.collectAsState()
	val selection by viewModel.selectedArtist.collectAsState()

	val starredState by viewModel.starredState.collectAsState()

	RefreshBox(
		modifier = Modifier.background(MaterialTheme.colorScheme.surface),
		isRefreshing = artistsState is UiState.Loading,
		onRefresh = { viewModel.refreshArtists() }
	) {
		AnimatedContent(artistsState) {
			when (it) {
				is UiState.Loading -> ArtGridPlaceholder()
				is UiState.Success -> {
					val grouped = it.data.flatMap { section ->
						section.artist.groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
							.toList()
							.sortedBy { it.first }
					}

					LazyVerticalGrid(
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
						grouped.forEach { (letter, artists) ->
							item(span = { GridItemSpan(maxLineSpan) }) {
								Text(
									text = letter.toString(),
									style = MaterialTheme.typography.headlineMedium,
									modifier = Modifier.padding(
										top = 16.dp,
										bottom = 8.dp
									)
								)
							}
							items(artists) { artist ->
								Box {
									ArtGridItem(
										modifier = Modifier.combinedClickable(
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
						}
					}
				}

				is UiState.Error -> ErrorBox(it)
			}
		}
	}
}
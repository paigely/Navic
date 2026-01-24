package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.arrow_back
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.common.TrackRow
import paige.navic.ui.viewmodel.SearchViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Artist
import paige.subsonic.api.model.Track

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
	searchBarState: SearchBarState,
	viewModel: SearchViewModel = viewModel { SearchViewModel() },
	enabled: Boolean
) {
	if (!enabled) return
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val state by viewModel.searchState.collectAsState()
	val scope = rememberCoroutineScope()

	ExpandedFullScreenSearchBar(
		state = searchBarState,
		collapsedShape = RectangleShape,
		inputField = {
			SearchBarDefaults.InputField(
				textFieldState = viewModel.searchQuery,
				searchBarState = searchBarState,
				colors = SearchBarDefaults.appBarWithSearchColors().searchBarColors.inputFieldColors,
				onSearch = {},
				leadingIcon = {
					IconButton({
						scope.launch { searchBarState.animateToCollapsed() }
					}) {
						Icon(
							vectorResource(Res.drawable.arrow_back),
							stringResource(Res.string.action_navigate_back),
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			)
		},
		colors = SearchBarDefaults.colors(
			dividerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
			containerColor = MaterialTheme.colorScheme.surface
		)
	) {
		AnimatedContent(
			state,
			modifier = Modifier.fillMaxSize()
		) {
			when (it) {
				is UiState.Loading -> ArtGrid {
					artGridPlaceholder()
				}
				is UiState.Error -> ErrorBox(it)
				is UiState.Success -> {
					val results = it.data

					val albums = results.filterIsInstance<Album>()
					val artists = results.filterIsInstance<Artist>()
					val tracks = results.filterIsInstance<Track>()

					val scrollState = rememberScrollState()

					Column(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(scrollState),
						verticalArrangement = Arrangement.spacedBy(20.dp)
					) {
						Spacer(Modifier.height(0.dp))
						ArtCarousel(Res.string.title_albums, albums) { album ->
							ArtCarouselItem(album.coverArt, album.name) {
								backStack.add(Screen.Tracks(album))
							}
						}
						ArtCarousel(Res.string.title_artists, artists) { artist ->
							ArtCarouselItem(artist.coverArt, artist.name) {
								backStack.add(Screen.Artist(artist.id))
							}
						}
						Column {
							Text(
								stringResource(Res.string.title_songs),
								style = MaterialTheme.typography.headlineSmall,
								modifier = Modifier.padding(horizontal = 20.dp)
							)
							tracks.forEach { track ->
								TrackRow(track = track)
							}
						}
					}
				}
			}
		}
	}
}

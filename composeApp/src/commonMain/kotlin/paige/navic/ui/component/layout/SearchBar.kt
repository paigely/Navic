package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExpandedFullScreenSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.arrow_back
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.ui.component.common.ErrorBox
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
					val player = LocalMediaPlayer.current

					val scrollState = rememberScrollState()

					Column(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(scrollState),
						verticalArrangement = Arrangement.spacedBy(20.dp)
					) {
						Spacer(Modifier.height(0.dp))
						SearchSection(Res.string.title_albums, albums) { album ->
							SearchSectionItem(album.coverArt, album.name) {
								backStack.add(Screen.Tracks(album))
							}
						}
						SearchSection(Res.string.title_artists, artists) { artist ->
							SearchSectionItem(artist.coverArt, artist.name)
						}
						Column {
							Text(
								stringResource(Res.string.title_songs),
								style = MaterialTheme.typography.headlineSmall,
								modifier = Modifier.padding(horizontal = 20.dp)
							)
							tracks.forEach { track ->
								ListItem(
									modifier = Modifier.clickable {
										ctx.clickSound()
										player.playSingle(track)
									},
									headlineContent = {
										Text(track.title)
									},
									supportingContent = {
										Text(
											buildString {
												append(track.album ?: "Unknown album")
												append(" • ")
												append(track.artist ?: "Unknown artist(s)")
												append(" • ")
												append(track.year ?: "Unknown year")
											},
											maxLines = 1
										)
									},
									leadingContent = {
										AsyncImage(
											model = track.coverArt,
											contentDescription = null,
											modifier = Modifier
												.padding(start = 6.5.dp)
												.size(50.dp)
												.clip(ContinuousRoundedRectangle(8.dp)),
											contentScale = ContentScale.Crop
										)
									}
								)
							}
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SearchSection(
	title: StringResource,
	items: List<T>,
	content: @Composable CarouselItemScope.(item: T) -> Unit
) {
	if (items.isNotEmpty()) {
		val state = rememberCarouselState { items.count() }
		Column(Modifier.padding(horizontal = 20.dp)) {
			Text(
				stringResource(title),
				style = MaterialTheme.typography.headlineSmall
			)
			HorizontalMultiBrowseCarousel(
				state = state,
				flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(
					state = state
				),
				modifier = Modifier
					.fillMaxWidth()
					.wrapContentHeight()
					.padding(top = 16.dp, bottom = 16.dp),
				preferredItemWidth = 150.dp,
				itemSpacing = 8.dp
			) { index ->
				content(items[index])
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarouselItemScope.SearchSectionItem(
	image: String?,
	contentDescription: String?,
	onClick: () -> Unit = {}
) {
	val ctx = LocalCtx.current
	val focusManager = LocalFocusManager.current
	AsyncImage(
		model = image,
		contentDescription = contentDescription,
		modifier = Modifier
			.size(150.dp)
			.maskClip(ContinuousRoundedRectangle(15.dp))
			.clickable {
				ctx.clickSound()
				focusManager.clearFocus(true)
				onClick()
			},
		contentScale = ContentScale.Crop
	)
}

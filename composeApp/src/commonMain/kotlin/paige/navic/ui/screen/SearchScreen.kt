package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_clear_search
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_all
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_search
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalImageBuilder
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ArrowBack
import paige.navic.icons.outlined.Close
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.component.layout.horizontalSection
import paige.navic.ui.viewmodel.AlbumsViewModel
import paige.navic.ui.viewmodel.ArtistsViewModel
import paige.navic.ui.viewmodel.SearchViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Artist
import paige.subsonic.api.model.ListType
import paige.subsonic.api.model.Track

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	viewModel: SearchViewModel = viewModel { SearchViewModel() }
) {
	val query = viewModel.searchQuery
	val state by viewModel.searchState.collectAsState()
	val ctx = LocalCtx.current
	val imageBuilder = LocalImageBuilder.current
	val player = LocalMediaPlayer.current

	val artistsViewModel = viewModel { ArtistsViewModel() }
	val albumsViewModel = viewModel { AlbumsViewModel(ListType.ALPHABETICAL_BY_NAME) }

	var selectedCategory by remember { mutableStateOf("All") }

	Column(
		modifier = Modifier.padding(top = 32.dp, bottom = LocalContentPadding.current.calculateBottomPadding())
	) {
		SearchTopBar(
			query = query,
			selectedCategory = selectedCategory,
			onCategorySelect = { selectedCategory = it }
		)
		AnimatedContent(
			state,
			modifier = Modifier.fillMaxSize()
		) { uiState ->
			when (uiState) {
				is UiState.Loading -> ArtGrid { artGridPlaceholder() }
				is UiState.Error -> ErrorBox(uiState)
				is UiState.Success -> {
					val results = uiState.data
					val showAll = selectedCategory == "All"
					val albums = if (showAll || selectedCategory == "Albums") results.filterIsInstance<Album>() else emptyList()
					val artists = if (showAll || selectedCategory == "Artists") results.filterIsInstance<Artist>() else emptyList()
					val tracks = if (showAll || selectedCategory == "Tracks") results.filterIsInstance<Track>() else emptyList()

					LazyVerticalGrid(
						modifier = Modifier.fillMaxSize(),
						columns = GridCells.Fixed(2),
						contentPadding = PaddingValues(bottom = 16.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						if (query.text.isNotBlank()) {
							if (tracks.isNotEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									Text(
										stringResource(Res.string.title_songs),
										style = MaterialTheme.typography.headlineSmall,
										modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
									)
								}
								items(tracks.take(10).size, span = { GridItemSpan(maxLineSpan) }) { index ->
									val track = tracks[index]
									ListItem(
										modifier = Modifier.clickable {
											ctx.clickSound()
											player.playSingle(track)
										},
										headlineContent = { Text(track.title) },
										supportingContent = {
											Text(
												"${track.album ?: ""} • ${track.artist ?: ""} • ${track.year ?: ""}",
												maxLines = 1
											)
										},
										leadingContent = {
											AsyncImage(
												model = imageBuilder
													.data(track.coverArt)
													.memoryCacheKey(track.coverArt)
													.diskCacheKey(track.coverArt)
													.diskCachePolicy(CachePolicy.ENABLED)
													.memoryCachePolicy(CachePolicy.ENABLED).build(),
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

							horizontalSection(
								title = Res.string.title_albums,
								destination = Screen.Albums(true),
								state = UiState.Success(albums),
								key = { it.id },
								seeAll = false
							) { album ->
								AlbumsScreenItem(
									modifier = Modifier.animateItem().width(150.dp),
									album = album,
									viewModel = albumsViewModel,
									onSetShareId = { }
								)
							}

							horizontalSection(
								title = Res.string.title_artists,
								destination = Screen.Artists(true),
								state = UiState.Success(artists),
								key = { it.id },
								seeAll = false
							) { artist ->
								ArtistsScreenItem(
									modifier = Modifier.animateItem().width(150.dp),
									artist = artist,
									viewModel = artistsViewModel
								)
							}
						}
					}
				}
			}
		}
		Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
	}
}

@Composable
private fun SearchTopBar(
	query: TextFieldState,
	selectedCategory: String,
	onCategorySelect: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	val focusManager = LocalFocusManager.current
	val focusRequester = remember { FocusRequester() }

	val shape = RoundedCornerShape(12.dp)
	val categories = listOf(Res.string.title_all, Res.string.title_songs, Res.string.title_albums, Res.string.title_artists)

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	Column {
		Row(
			modifier = Modifier
				.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
				.height(IntrinsicSize.Min),
			verticalAlignment = Alignment.CenterVertically,
		) {
			Column(
				modifier = Modifier
					.width(50.dp)
					.fillMaxHeight()
					.clip(shape)
					.background(MaterialTheme.colorScheme.surfaceContainer, shape)
					.clickable(
						onClick = {
							ctx.clickSound()
							focusManager.clearFocus(true)
							if (backStack.size > 1) backStack.removeLastOrNull()
						}
					),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally,
			) {
				Icon(
					Icons.Outlined.ArrowBack,
					contentDescription = stringResource(Res.string.action_navigate_back),
					tint = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			Spacer(Modifier.width(8.dp))
			TextField(
				modifier = Modifier
					.fillMaxWidth()
					.background(MaterialTheme.colorScheme.surfaceContainer, shape)
					.focusRequester(focusRequester),
				state = query,
				lineLimits = TextFieldLineLimits.SingleLine,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
				onKeyboardAction = { focusManager.clearFocus() },
				placeholder = { Text(stringResource(Res.string.title_search)) },
				trailingIcon = {
					if (query.text.isNotEmpty()) {
						IconButton(
							onClick = {
								ctx.clickSound()
								query.clearText()
							}
						) {
							Icon(
								Icons.Outlined.Close,
								contentDescription = stringResource(Res.string.action_clear_search)
							)
						}
					}
				},
				colors = TextFieldDefaults.colors(
					focusedContainerColor = Color.Transparent,
					unfocusedContainerColor = Color.Transparent,
					focusedIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				)
			)
		}
		LazyRow(
			contentPadding = PaddingValues(horizontal = 20.dp),
			horizontalArrangement = Arrangement.spacedBy(8.dp)
		) {
			items(categories) { category ->
				val isSelected = category.toString() == selectedCategory
				OutlinedButton(
					onClick = {
						ctx.clickSound()
						onCategorySelect(category.toString())
					},
					shape = shape,
					colors = ButtonDefaults.outlinedButtonColors(
						containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
						contentColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
					),
					border = if (isSelected) null else BorderStroke(
						1.dp,
						MaterialTheme.colorScheme.onSurfaceVariant
					),
					elevation = ButtonDefaults.buttonElevation(0.dp)
				) {
					Text(stringResource(category))
				}
			}
		}
	}
}

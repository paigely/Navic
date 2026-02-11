package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
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
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalImageBuilder
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ArrowBack
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Close
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.components.layouts.horizontalSection
import paige.navic.ui.viewmodels.AlbumsViewModel
import paige.navic.ui.viewmodels.ArtistsViewModel
import paige.navic.ui.viewmodels.SearchViewModel
import paige.navic.utils.UiState
import paige.subsonic.api.models.Album
import paige.subsonic.api.models.Artist
import paige.subsonic.api.models.ListType
import paige.subsonic.api.models.Track

enum class SearchCategory(val res: StringResource) {
	ALL(Res.string.title_all),
	SONGS(Res.string.title_songs),
	ALBUMS(Res.string.title_albums),
	ARTISTS(Res.string.title_artists)
}

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

	var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }

	Column(
		modifier = Modifier.padding(
			top = LocalContentPadding.current.calculateTopPadding(),
			bottom = LocalContentPadding.current.calculateBottomPadding()
		)
	) {
		SearchTopBar(query = query)

		SearchChips(
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
					val showAll = selectedCategory == SearchCategory.ALL
					val albums =
						if (showAll || selectedCategory == SearchCategory.ALBUMS) results.filterIsInstance<Album>() else emptyList()
					val artists =
						if (showAll || selectedCategory == SearchCategory.ARTISTS) results.filterIsInstance<Artist>() else emptyList()
					val tracks =
						if (showAll || selectedCategory == SearchCategory.SONGS) results.filterIsInstance<Track>() else emptyList()

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
										modifier = Modifier.padding(
											horizontal = 20.dp,
											vertical = 8.dp
										)
									)
								}
								items(
									tracks.take(10).size,
									span = { GridItemSpan(maxLineSpan) }) { index ->
									val track = tracks[index]
									ListItem(
										modifier = Modifier.clickable {
											ctx.clickSound()
											player.playSingle(track)
										},
										headlineContent = { Text(track.title) },
										supportingContent = {
											MarqueeText(
												"${track.album ?: ""} • ${track.artist ?: ""} • ${track.year ?: ""}"
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SearchChips(
	selectedCategory: SearchCategory,
	onCategorySelect: (SearchCategory) -> Unit
) {
	val ctx = LocalCtx.current
	Row(
		modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 600.dp),
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		SearchCategory.entries.forEach { category ->
			val isSelected = category == selectedCategory
			FilterChip(
				modifier = Modifier
					.animateContentSize(
						if (isSelected)
							MaterialTheme.motionScheme.fastSpatialSpec()
						else MaterialTheme.motionScheme.defaultEffectsSpec()
					),
				selected = isSelected,
				onClick = {
					ctx.clickSound()
					onCategorySelect(category)
				},
				label = {
					Text(
						stringResource(category.res),
						maxLines = 1
					)
				},
				shape = ContinuousRoundedRectangle(8.dp),
				leadingIcon = if (isSelected) {
					{
						Icon(
							imageVector = Icons.Outlined.Check,
							contentDescription = null,
							modifier = Modifier.size(FilterChipDefaults.IconSize)
						)
					}
				} else {
					null
				}
			)
		}
	}
}

@Composable
private fun SearchTopBar(
	query: TextFieldState
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	val focusManager = LocalFocusManager.current
	val focusRequester = remember { FocusRequester() }

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	Column {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier.size(56.dp),
				contentAlignment = Alignment.Center
			) {
				IconButton(
					onClick = {
						ctx.clickSound()
						focusManager.clearFocus(true)
						if (backStack.size > 1) backStack.removeLastOrNull()
					}
				) {
					Icon(
						Icons.Outlined.ArrowBack,
						contentDescription = stringResource(Res.string.action_navigate_back),
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
				}
			}
			BasicTextField(
				state = query,
				modifier = Modifier
					.fillMaxWidth()
					.height(72.dp)
					.focusRequester(focusRequester),
				lineLimits = TextFieldLineLimits.SingleLine,
				keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
				onKeyboardAction = { focusManager.clearFocus() },
				textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
				cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
				decorator = { innerTextField ->
					Box(contentAlignment = Alignment.CenterStart) {
						if (query.text.isEmpty()) {
							Text(
								text = stringResource(Res.string.title_search),
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						innerTextField()
					}
				}
			)
			Box(
				modifier = Modifier.size(56.dp),
				contentAlignment = Alignment.Center
			) {
				if (query.text.isNotEmpty()) {
					IconButton(
						modifier = Modifier.padding(horizontal = 8.dp),
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
			}
		}
	}
}

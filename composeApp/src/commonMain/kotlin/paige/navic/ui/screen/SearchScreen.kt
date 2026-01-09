package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_clear_search
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.arrow_back
import navic.composeapp.generated.resources.close
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_search
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.Tracks
import paige.navic.ui.component.ArtGridPlaceholder
import paige.navic.ui.component.ErrorBox
import paige.navic.ui.viewmodel.SearchViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Artist

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	viewModel: SearchViewModel = viewModel { SearchViewModel() }
) {
	val query by viewModel.searchQuery.collectAsState()
	val state by viewModel.searchState.collectAsState()
	val backStack = LocalNavStack.current

	Column {
		SearchTopBar(
			query = query,
			onQueryChange = {
				viewModel.search(it.text)
			}
		)
		AnimatedContent(state) {
			when (it) {
				is UiState.Loading -> ArtGridPlaceholder()
				is UiState.Error -> ErrorBox(it)
				is UiState.Success -> {
					val results = it.data

					val albums = results.filterIsInstance<Album>()
					val artists = results.filterIsInstance<Artist>()

					val scrollState = rememberScrollState()

					Column(
						modifier = Modifier
							.fillMaxSize()
							.padding(
								bottom = 117.9.dp,
								start = 20.dp,
								end = 20.dp
							)
							.verticalScroll(scrollState),
						verticalArrangement = Arrangement.spacedBy(20.dp)
					) {
						SearchSection(Res.string.title_albums, albums) { album ->
							SearchSectionItem(album.coverArt, album.name) {
								backStack.add(Tracks(album))
							}
						}
						SearchSection(Res.string.title_artists, artists) { artist ->
							SearchSectionItem(artist.coverArt, artist.name)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T>SearchSection(
	title: StringResource,
	items: List<T>,
	content: @Composable CarouselItemScope.(item: T) -> Unit
) {
	if (items.isNotEmpty()) {
		Column {
			Text(
				stringResource(title),
				style = MaterialTheme.typography.headlineSmall
			)
			HorizontalMultiBrowseCarousel(
				state = rememberCarouselState { items.count() },
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
	AsyncImage(
		model = image,
		contentDescription = contentDescription,
		modifier = Modifier
			.size(150.dp)
			.maskClip(ContinuousRoundedRectangle(15.dp))
			.clickable {
				ctx.clickSound()
				onClick()
			},
		contentScale = ContentScale.Crop
	)
}

@Composable
private fun SearchTopBar(
	query: String,
	onQueryChange: (TextFieldValue) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	val focusRequester = remember { FocusRequester() }
	val queryValue = remember(query) {
		mutableStateOf(
			TextFieldValue(
				query,
				TextRange(query.length)
			)
		)
	}

	LaunchedEffect(Unit) {
		focusRequester.requestFocus()
	}

	Row(
		Modifier.padding(20.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Column(
			modifier = Modifier
				.size(50.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
				.clickable(
					onClick = {
						ctx.clickSound()
						backStack.removeLast()
					}
				),
			verticalArrangement = Arrangement.Center,
			horizontalAlignment = Alignment.CenterHorizontally,
		) {
			Icon(
				vectorResource(Res.drawable.arrow_back),
				contentDescription = stringResource(Res.string.action_navigate_back),
				tint = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		Spacer(Modifier.width(8.dp))
		TextField(
			modifier = Modifier
				.fillMaxWidth()
				.heightIn(min = 50.dp)
				.background(MaterialTheme.colorScheme.surfaceContainer, ContinuousCapsule)
				.focusRequester(focusRequester),
			value = queryValue.value,
			onValueChange = onQueryChange,
			placeholder = { Text(stringResource(Res.string.title_search)) },
			trailingIcon = {
				if (query.isNotEmpty()) {
					IconButton(
						onClick = {
							ctx.clickSound()
							onQueryChange(TextFieldValue())
						}
					) {
						Icon(
							vectorResource(Res.drawable.close),
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
}
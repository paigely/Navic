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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_artist
import navic.composeapp.generated.resources.info_unknown_year
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_search
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.component.layout.ArtCarousel
import paige.navic.ui.component.layout.ArtCarouselItem
import paige.navic.ui.component.layout.ArtGrid
import paige.navic.ui.component.layout.artGridPlaceholder
import paige.navic.ui.viewmodel.SearchViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Artist
import paige.subsonic.api.model.Track

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	viewModel: SearchViewModel = viewModel { SearchViewModel() }
) {
	val query by viewModel.searchQuery.collectAsState()
	val state by viewModel.searchState.collectAsState()
	val backStack = LocalNavStack.current
	val ctx = LocalCtx.current

	Column(
		modifier = Modifier.padding(top = 32.dp)
	) {
		SearchTopBar(
			query = query,
			onQueryChange = {
				viewModel.search(it)
			}
		)
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
						if(query.isNotBlank() && tracks.isNotEmpty()) {
							Column {
								Text(
									stringResource(Res.string.title_songs),
									style = MaterialTheme.typography.headlineSmall,
									modifier = Modifier.padding(horizontal = 20.dp)
								)
								tracks.take(10).forEach { track ->
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
													append(track.album ?: stringResource(Res.string.info_unknown_album))
													append(" • ")
													append(track.artist ?: stringResource(Res.string.info_unknown_artist))
													append(" • ")
													append(track.year ?: stringResource(Res.string.info_unknown_year))
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
						Spacer(Modifier.height(100.dp))
					}
				}
			}
		}
	}
}

@Composable
private fun SearchTopBar(
	query: String,
	onQueryChange: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	val focusManager = LocalFocusManager.current
	val focusRequester = remember { FocusRequester() }

	var textFieldValue by remember {
		mutableStateOf(TextFieldValue(query, TextRange(query.length)))
	}

	LaunchedEffect(query) {
		if (query != textFieldValue.text) {
			textFieldValue = TextFieldValue(query, TextRange(query.length))
		}
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
						focusManager.clearFocus(true)
						if (backStack.size > 1) backStack.removeLastOrNull()
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
			value = textFieldValue,
			onValueChange = {
				textFieldValue = it
				onQueryChange(it.text)
			},
			placeholder = { Text(stringResource(Res.string.title_search)) },
			trailingIcon = {
				if (textFieldValue.text.isNotEmpty()) {
					IconButton(
						onClick = {
							ctx.clickSound()
							textFieldValue = TextFieldValue("", TextRange(0))
							onQueryChange("")
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
			),
			maxLines = 1
		)
	}
}

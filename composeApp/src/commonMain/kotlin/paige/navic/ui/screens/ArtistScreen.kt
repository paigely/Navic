package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.minus
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_view_on_lastfm
import navic.composeapp.generated.resources.action_view_on_musicbrainz
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_similar_artists
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.brand.Lastfm
import paige.navic.icons.brand.Musicbrainz
import paige.navic.icons.outlined.MoreVert
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.TrackRow
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.viewmodels.ArtistViewModel
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistScreen(
	artistId: String,
	viewModel: ArtistViewModel = viewModel(key = artistId) { ArtistViewModel(artistId) }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val uriHandler = LocalUriHandler.current
	val artistState by viewModel.artistState.collectAsState()

	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

	Scaffold(
		topBar = {
			val state = (artistState as? UiState.Success)?.data
			if (state != null) {
				NestedTopBar(
					colors = TopAppBarDefaults.topAppBarColors(
						containerColor = Color.Transparent
					),
					title = {},
					actions = {
						Box {
							var expanded by remember { mutableStateOf(false) }
							TopBarButton({
								expanded = true
							}) {
								Icon(
									Icons.Outlined.MoreVert,
									stringResource(Res.string.action_more)
								)
							}
							Dropdown(
								expanded = expanded,
								onDismissRequest = { expanded = false }
							) {
								DropdownItem(
									text = { Text(stringResource(Res.string.action_view_on_lastfm)) },
									leadingIcon = { Icon(Icons.Brand.Lastfm, null) },
									enabled = state.info.lastFmUrl != null,
									onClick = {
										expanded = false
										state.info.lastFmUrl?.let { url ->
											uriHandler.openUri(url)
										}
									}
								)
								DropdownItem(
									text = { Text(stringResource(Res.string.action_view_on_musicbrainz)) },
									leadingIcon = { Icon(Icons.Brand.Musicbrainz, null) },
									enabled = state.info.musicBrainzId != null,
									onClick = {
										expanded = false
										state.info.musicBrainzId?.let { id ->
											uriHandler.openUri(
												"https://musicbrainz.org/artist/$id"
											)
										}
									}
								)
							}
						}
					}
				)
			} else {
				NestedTopBar({})
			}
		}
	) { innerPadding ->
		AnimatedContent(
			targetState = artistState,
			transitionSpec = {
				(fadeIn(
					animationSpec = effectSpec
				) + scaleIn(
					initialScale = 0.8f,
					animationSpec = spatialSpec
				)) togetherWith (fadeOut(
					animationSpec = effectSpec
				) + scaleOut(
					animationSpec = spatialSpec
				))
			},
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding - PaddingValues(top = innerPadding.calculateTopPadding()))
		) {
			when (it) {
				is UiState.Error -> Box(Modifier.fillMaxSize()) {
					ErrorBox(it)
				}
				is UiState.Loading -> Box(Modifier.fillMaxSize()) {
					ContainedLoadingIndicator(Modifier.size(80.dp).align(Alignment.Center))
				}
				is UiState.Success -> {
					val state = it.data
					Column(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(rememberScrollState())
							.padding(bottom = 117.9.dp),
						verticalArrangement = Arrangement.spacedBy(12.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						ArtistHeader(
							artistName = state.artist.name,
							coverArt = state.artist.coverArt,
							subtitle = (artistState as? UiState.Success)?.data?.info?.biography,
							lastfm = (artistState as? UiState.Success)?.data?.info?.lastFmUrl
						)
						state.topSongs.takeIf { state.topSongs.isNotEmpty() }?.let { songs ->
							Text(
								stringResource(Res.string.option_sort_frequent),
								style = MaterialTheme.typography.titleMediumEmphasized,
								fontWeight = FontWeight(600),
								modifier = Modifier
									.heightIn(min = 32.dp)
									.padding(top = 8.dp)
									.padding(horizontal = 20.dp)
									.fillMaxWidth()
							)
							LazyHorizontalGrid(
								rows = GridCells.Fixed(3),
								modifier = Modifier.fillMaxWidth().height(250.dp)
							) {
								items(songs) { song ->
									TrackRow(
										modifier = Modifier.weight(1f),
										track = song
									)
								}
							}
						}
						state.artist.album?.let { albums ->
							ArtCarousel(
								Res.string.title_albums,
								albums.sortedByDescending { it.playCount }
							) { album ->
								ArtCarouselItem(album.coverArt, album.name) {
									backStack.add(Screen.Tracks(album))
								}
							}
						}
						state.info.similarArtist?.let { similarArtists ->
							Text(
								stringResource(Res.string.title_similar_artists),
								style = MaterialTheme.typography.titleMediumEmphasized,
								fontWeight = FontWeight(600),
								modifier = Modifier
									.height(32.dp)
									.padding(top = 8.dp)
									.padding(horizontal = 20.dp)
									.fillMaxWidth()
							)
							LazyRow(
								modifier = Modifier.fillMaxWidth().animateContentSize(
									animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()
								),
								horizontalArrangement = Arrangement.spacedBy(8.dp),
								contentPadding = PaddingValues(horizontal = 20.dp)
							) {
								items(similarArtists) { artist ->
									ArtGridItem(
										imageModifier = Modifier.size(150.dp),
										onClick = {
											ctx.clickSound()
											backStack.add(Screen.Artist(artist.id))
										},
										coverArt = artist.coverArt,
										title = artist.name
									)
								}
							}
						}
						Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
					}
				}
			}
		}
	}
}

fun truncateText(text: String, limit: Int): String {
	return if (text.length > limit) {
		text.take(limit) + "..."
	} else {
		text
	}
}


@Composable
fun ArtistHeader(
	artistName: String,
	coverArt: String?,
	subtitle: String?,
	lastfm: String?
) {
	val platformContext = LocalPlatformContext.current
	val model = remember(coverArt) {
		ImageRequest.Builder(platformContext)
			.data(SessionManager.api.getCoverArtUrl(coverArt, auth = true))
			.memoryCacheKey(coverArt)
			.diskCacheKey(coverArt)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.crossfade(500)
			.build()
	}
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(400.dp)
			.background(MaterialTheme.colorScheme.surfaceContainer)
	) {
		AsyncImage(
			model = model,
			contentDescription = null,
			contentScale = ContentScale.Crop,
			modifier = Modifier.fillMaxSize()
		)
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.linearGradient(
						colors = listOf(Color.Black, Color.Transparent),
						start = Offset(0f, Float.POSITIVE_INFINITY),
						end = Offset(Float.POSITIVE_INFINITY, 0f)
					)
				)
		)
		Box(
			modifier = Modifier
				.align(Alignment.BottomCenter)
				.fillMaxWidth()
				.height(2.dp)
				.background(Color.White.copy(alpha = .1f))
		)

		Column(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(horizontal = 20.dp, vertical = 24.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			subtitle?.let { subtitle ->
				Text(
					text = buildAnnotatedString {
						append(truncateText(subtitle, 200))
						if (subtitle.length > 200 && lastfm != null) {
							append(" ")
							withLink(LinkAnnotation.Url(lastfm)) {
								append(stringResource(Res.string.action_more))
							}
						}
					},
					style = MaterialTheme.typography.bodySmall,
					color = Color.LightGray
				)
			}
			Text(
				text = artistName,
				style = MaterialTheme.typography.displaySmall,
				fontWeight = FontWeight.Bold,
				color = Color.White
			)
		}
	}
}
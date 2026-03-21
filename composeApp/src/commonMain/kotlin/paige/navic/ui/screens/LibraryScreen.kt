package paige.navic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import dev.zt64.subsonic.api.model.AlbumListType
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_genres
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.History
import paige.navic.icons.outlined.LibraryAdd
import paige.navic.icons.outlined.Shuffle
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.dialogs.ShareDialog
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.horizontalSection
import paige.navic.ui.screens.genres.GenreCard
import paige.navic.ui.theme.defaultFont
import paige.navic.ui.viewmodels.AlbumsViewModel
import paige.navic.ui.viewmodels.ArtistsViewModel
import paige.navic.ui.viewmodels.GenresViewModel
import paige.navic.ui.viewmodels.PlaylistsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
	albumsViewModel: AlbumsViewModel = viewModel(key = "libraryAlbums") {
		AlbumsViewModel(AlbumListType.Recent)
	},
	playlistsViewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel() },
	artistsViewModel: ArtistsViewModel = viewModel { ArtistsViewModel() },
	genresViewModel: GenresViewModel = viewModel { GenresViewModel() }
) {
	val recentsState by albumsViewModel.albumsState.collectAsState()
	val playlistsState by playlistsViewModel.playlistsState.collectAsState()
	val artistsState by artistsViewModel.artistsState.collectAsState()
	val genresState by genresViewModel.genresState.collectAsState()

	val gridState = albumsViewModel.gridState

	val flatArtistsState = remember(artistsState) {
		when (val s = artistsState) {
			is UiState.Success -> UiState.Success(s.data)
			is UiState.Loading -> UiState.Loading
			is UiState.Error -> UiState.Error(s.error)
		}
	}

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }
	val isLoggedIn by SessionManager.isLoggedIn.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = { RootTopBar({ Text(stringResource(Res.string.title_library)) }, scrollBehavior) },
		bottomBar = { RootBottomBar(scrolled = gridState.lastScrolledForward) }
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = recentsState is UiState.Loading
				|| playlistsState is UiState.Loading
				|| artistsState is UiState.Loading
				|| genresState is UiState.Loading,
			onRefresh = {
				if (!isLoggedIn) return@PullToRefreshBox
				albumsViewModel.refreshAlbums()
				playlistsViewModel.refreshPlaylists()
				artistsViewModel.refreshArtists()
				genresViewModel.refreshGenres()
			}
		) {
			LazyVerticalGrid(
				modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
				state = gridState,
				columns = GridCells.Fixed(2),
				contentPadding = innerPadding.withoutTop(),
				verticalArrangement = Arrangement.spacedBy(5.dp),
				horizontalArrangement = Arrangement.spacedBy(5.dp),
			) {
				overviewButton(
					icon = Icons.Outlined.LibraryAdd,
					label = Res.string.option_sort_newest,
					destination = Screen.Albums(true, AlbumListType.Newest),
					start = true
				)
				overviewButton(
					icon = Icons.Outlined.Shuffle,
					label = Res.string.option_sort_random,
					destination = Screen.Albums(true, AlbumListType.Random),
					start = false
				)
				overviewButton(
					icon = Icons.Outlined.Star,
					label = Res.string.option_sort_starred,
					destination = Screen.Albums(true, AlbumListType.Starred),
					start = true
				)
				overviewButton(
					icon = Icons.Outlined.History,
					label = Res.string.option_sort_frequent,
					destination = Screen.Albums(true, AlbumListType.Frequent),
					start = false
				)
				if (!isLoggedIn) {
					item(span = { GridItemSpan(maxLineSpan) }) {
						Text(
							stringResource(Res.string.info_needs_log_in),
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							modifier = Modifier.padding(horizontal = 16.dp)
						)
					}
				} else {
					horizontalSection(
						title = Res.string.option_sort_recent,
						destination = Screen.Albums(true, AlbumListType.Recent),
						state = recentsState,
						key = { it.id },
						seeAll = true
					) { album ->
						AlbumsScreenItem(
							modifier = Modifier.animateItem(fadeInSpec = null).width(150.dp),
							album = album,
							viewModel = albumsViewModel,
							onSetShareId = { shareId = it },
							tab = "library"
						)
					}
					horizontalSection(
						title = Res.string.title_playlists,
						destination = Screen.Playlists(true),
						state = playlistsState,
						key = { it.id },
						seeAll = true
					) { playlist ->
						PlaylistsScreenItem(
							modifier = Modifier.animateItem(fadeInSpec = null).width(150.dp),
							playlist = playlist,
							viewModel = playlistsViewModel,
							onSetShareId = { shareId = it },
							onSetDeletionId = { deletionId = it },
							tab = "library"
						)
					}

					horizontalSection(
						title = Res.string.title_artists,
						destination = Screen.Artists(true),
						state = flatArtistsState,
						key = { it.id },
						seeAll = true
					) { artist ->
						ArtistsScreenItem(
							modifier = Modifier.animateItem(fadeInSpec = null).width(150.dp),
							artist = artist,
							viewModel = artistsViewModel,
							tab = "library"
						)
					}

					horizontalSection(
						title = Res.string.title_genres,
						destination = Screen.Genres(true),
						state = genresState,
						key = { it.genre.name },
						seeAll = true
					) { genre ->
						GenreCard(genre = genre)
					}
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)

	@Suppress("AssignedValueIsNeverRead")
	DeletionDialog(
		endpoint = DeletionEndpoint.PLAYLIST,
		id = deletionId,
		onIdClear = { deletionId = null }
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyGridScope.overviewButton(
	icon: ImageVector,
	label: StringResource,
	destination: NavKey,
	start: Boolean
) {
	item(span = { GridItemSpan(1) }) {
		val ctx = LocalCtx.current
		val backStack = LocalNavStack.current
		Button(
			modifier = Modifier
				.fillMaxWidth()
				.height(42.dp)
				.padding(
					start = if (start) 16.dp else 0.dp,
					end = if (!start) 16.dp else 0.dp,
				),
			contentPadding = PaddingValues(horizontal = 12.dp),
			elevation = null,
			shapes = ButtonDefaults.shapes(
				shape = MaterialTheme.shapes.small,
				pressedShape = MaterialTheme.shapes.extraSmall
			),
			colors = ButtonDefaults.buttonColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				contentColor = MaterialTheme.colorScheme.onSurfaceVariant
			),
			onClick = {
				ctx.clickSound()
				if (backStack.lastOrNull() !is Screen.Albums) {
					backStack.add(destination)
				}
			}
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				verticalAlignment = Alignment.CenterVertically
			) {
				Icon(
					icon,
					contentDescription = null
				)
				Spacer(Modifier.width(10.dp))
				Text(
					stringResource(label),
					maxLines = 1,
					fontFamily = defaultFont(100, round = 100f),
					autoSize = TextAutoSize.StepBased(minFontSize = 1.sp, maxFontSize = 14.sp),
				)
			}
		}
	}
}
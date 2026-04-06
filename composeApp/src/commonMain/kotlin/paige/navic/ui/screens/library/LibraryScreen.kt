package paige.navic.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.models.Screen
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.History
import paige.navic.icons.outlined.LibraryAdd
import paige.navic.icons.outlined.Shuffle
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.horizontalSection
import paige.navic.ui.screens.album.components.AlbumListScreenItem
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.artist.ArtistsScreenItem
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.ui.screens.genre.components.GenreListScreenCard
import paige.navic.ui.screens.genre.viewmodels.GenreListViewModel
import paige.navic.ui.screens.library.components.libraryScreenOverviewButton
import paige.navic.ui.screens.playlist.components.PlaylistListScreenItem
import paige.navic.ui.screens.playlist.viewmodels.PlaylistListViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.ui.viewmodels.LoginViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.LoginState
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
	val albumListViewModel = koinViewModel<AlbumListViewModel>(
		key = "libraryAlbums",
		parameters = { parametersOf(DomainAlbumListType.Frequent) }
	)
	val albumListSelection by albumListViewModel.selectedAlbum.collectAsState()
	val albumListStarred by albumListViewModel.starred.collectAsState()

	val playlistListViewModel = koinViewModel<PlaylistListViewModel>()
	val playlistListSelection by playlistListViewModel.selectedPlaylist.collectAsState()

	val artistListViewModel = koinViewModel<ArtistListViewModel>()
	val artistListSelection by artistListViewModel.selectedArtist.collectAsState()
	val artistListStarred by artistListViewModel.starred.collectAsState()

	val genreListViewModel = koinViewModel<GenreListViewModel>()

	val recentsState by albumListViewModel.albumsState.collectAsState()
	val playlistsState by playlistListViewModel.playlistsState.collectAsState()
	val artistsState by artistListViewModel.artistsState.collectAsState()
	val genresState by genreListViewModel.genresState.collectAsState()

	val loginViewModel = koinViewModel<LoginViewModel>()
	val loginState by loginViewModel.loginState.collectAsState()

	val gridState = albumListViewModel.gridState

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }
	val isLoggedIn by SessionManager.isLoggedIn.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	LaunchedEffect(loginState is LoginState.Success) {
		albumListViewModel.refreshAlbums(false)
		playlistListViewModel.refreshPlaylists(false)
		artistListViewModel.refreshArtists(false)
		genreListViewModel.refreshGenres(false)
	}

	Scaffold(
		topBar = { RootTopBar({ Text(stringResource(Res.string.title_library)) }, scrollBehavior) },
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			RootBottomBar(scrolled = scrollManager.isTriggered)
		}
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
				albumListViewModel.refreshAlbums(true)
				playlistListViewModel.refreshPlaylists(true)
				artistListViewModel.refreshArtists(true)
				genreListViewModel.refreshGenres(true)
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
				libraryScreenOverviewButton(
					icon = Icons.Outlined.LibraryAdd,
					label = Res.string.option_sort_newest,
					destination = Screen.AlbumList(true, DomainAlbumListType.Newest),
					start = true
				)
				libraryScreenOverviewButton(
					icon = Icons.Outlined.Shuffle,
					label = Res.string.option_sort_random,
					destination = Screen.AlbumList(true, DomainAlbumListType.Random),
					start = false
				)
				libraryScreenOverviewButton(
					icon = Icons.Outlined.Star,
					label = Res.string.option_sort_starred,
					destination = Screen.AlbumList(true, DomainAlbumListType.Starred),
					start = true
				)
				libraryScreenOverviewButton(
					icon = Icons.Outlined.History,
					label = Res.string.option_sort_frequent,
					destination = Screen.AlbumList(true, DomainAlbumListType.Frequent),
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
						destination = Screen.AlbumList(true, DomainAlbumListType.Recent),
						state = recentsState,
						key = { it.id },
						seeAll = true
					) { album ->
						AlbumListScreenItem(
							modifier = Modifier.animateItem(fadeInSpec = null).width(150.dp),
							tab = "library",
							album = album,
							selected = album == albumListSelection,
							starred = albumListStarred,
							onSelect = { albumListViewModel.selectAlbum(album) },
							onDeselect = { albumListViewModel.selectAlbum(null) },
							onSetStarred = { albumListViewModel.starAlbum(it) },
							onSetShareId = { shareId = it }
						)
					}
					horizontalSection(
						title = Res.string.title_playlists,
						destination = Screen.PlaylistList(true),
						state = playlistsState,
						key = { it.id },
						seeAll = true
					) { playlist ->
						PlaylistListScreenItem(
							modifier = Modifier.animateItem(fadeInSpec = null).width(150.dp),
							playlist = playlist,
							selected = playlist == playlistListSelection,
							onSelect = { playlistListViewModel.selectPlaylist(playlist) },
							onDeselect = { playlistListViewModel.clearSelection() },
							onSetShareId = { shareId = it },
							onSetDeletionId = { deletionId = it },
							tab = "library"
						)
					}

					horizontalSection(
						title = Res.string.title_artists,
						destination = Screen.ArtistList(true),
						state = artistsState,
						key = { it.id },
						seeAll = true
					) { artist ->
						ArtistsScreenItem(
							modifier = Modifier.animateItem(fadeInSpec = null).width(150.dp),
							tab = "library",
							artist = artist,
							selected = artist == artistListSelection,
							starred = artistListStarred,
							onSelect = { artistListViewModel.selectArtist(artist) },
							onDeselect = { artistListViewModel.clearSelection() },
							onSetStarred = { artistListViewModel.starArtist(it) }
						)
					}

					horizontalSection(
						title = Res.string.title_genres,
						destination = Screen.GenreList(true),
						state = genresState,
						key = { it.name },
						seeAll = true
					) { genreWithAlbums ->
						GenreListScreenCard(genre = genreWithAlbums)
					}
				}
			}
		}
	}

	val flattenedErrors = listOf(
		(recentsState as? UiState.Error)?.error,
		(playlistsState as? UiState.Error)?.error,
		(artistsState as? UiState.Error)?.error,
		(genresState as? UiState.Error)?.error
	).mapNotNull { it?.stackTraceToString() }.takeIf { it.isNotEmpty() }?.joinToString("\n\n")

	ErrorSnackbar(
		error = flattenedErrors?.let { Error(it) },
		onClearError = {
			albumListViewModel.clearError()
			playlistListViewModel.clearError()
			artistListViewModel.clearError()
			genreListViewModel.clearError()
		}
	)

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
		onIdClear = { deletionId = null },
		onRefresh = { playlistListViewModel.refreshPlaylists(true) }
	)
}

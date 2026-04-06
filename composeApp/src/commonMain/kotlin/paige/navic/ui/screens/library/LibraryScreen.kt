package paige.navic.ui.screens.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_create_playlist
import navic.composeapp.generated.resources.title_genres
import navic.composeapp.generated.resources.title_home
import navic.composeapp.generated.resources.title_library
import navic.composeapp.generated.resources.title_playlists
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarCollapseMode
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.ui.screens.genre.viewmodels.GenreListViewModel
import paige.navic.ui.screens.library.components.LibraryScreenTabRow
import paige.navic.ui.screens.library.tabs.LibraryScreenAlbumsTab
import paige.navic.ui.screens.library.tabs.LibraryScreenArtistsTab
import paige.navic.ui.screens.library.tabs.LibraryScreenGenresTab
import paige.navic.ui.screens.library.tabs.LibraryScreenHomeTab
import paige.navic.ui.screens.library.tabs.LibraryScreenPlaylistsTab
import paige.navic.ui.screens.library.tabs.LibraryScreenSongsTab
import paige.navic.ui.screens.playlist.dialogs.PlaylistCreateDialog
import paige.navic.ui.screens.playlist.viewmodels.PlaylistListViewModel
import paige.navic.ui.screens.song.viewmodels.SongListViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.ui.viewmodels.LoginViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.LoginState
import paige.navic.utils.UiState
import kotlin.time.Duration

enum class LibraryScreenTab(val title: StringResource) {
	Home(Res.string.title_home),
	Albums(Res.string.title_albums),
	Artists(Res.string.title_artists),
	Playlists(Res.string.title_playlists),
	Songs(Res.string.title_songs),
	Genres(Res.string.title_genres)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen() {
	val albumsViewModel = koinViewModel<AlbumListViewModel>(
		key = "libraryAlbums",
		parameters = { parametersOf(DomainAlbumListType.Recent) }
	)
	val albumsState by albumsViewModel.albumsState.collectAsStateWithLifecycle()
	val selectedAlbum by albumsViewModel.selectedAlbum.collectAsStateWithLifecycle()
	val selectedAlbumIsStarred by albumsViewModel.starred.collectAsStateWithLifecycle()
	val albumsListType by albumsViewModel.listType.collectAsStateWithLifecycle()

	val playlistsViewModel = koinViewModel<PlaylistListViewModel>()
	val playlistsState by playlistsViewModel.playlistsState.collectAsStateWithLifecycle()
	val selectedPlaylist by playlistsViewModel.selectedPlaylist.collectAsStateWithLifecycle()
	val playlistsListType by playlistsViewModel.sortMode.collectAsStateWithLifecycle()

	val artistsViewModel = koinViewModel<ArtistListViewModel>()
	val artistsState by artistsViewModel.artistsState.collectAsStateWithLifecycle()
	val selectedArtist by artistsViewModel.selectedArtist.collectAsStateWithLifecycle()
	val selectedArtistIsStarred by artistsViewModel.starred.collectAsStateWithLifecycle()
	val artistsListType by artistsViewModel.listType.collectAsStateWithLifecycle()

	val genresViewModel = koinViewModel<GenreListViewModel>()
	val genresState by genresViewModel.genresState.collectAsStateWithLifecycle()

	val songsViewModel = koinViewModel<SongListViewModel>()
	val songsState by songsViewModel.songsState.collectAsStateWithLifecycle()
	val selectedSong by songsViewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by songsViewModel.starred.collectAsStateWithLifecycle()
	val songsListType by songsViewModel.listType.collectAsStateWithLifecycle()

	val loginViewModel = koinViewModel<LoginViewModel>()
	val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()

	val ctx = LocalCtx.current
	var shareId by rememberSaveable { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var playlistDeletionId by rememberSaveable { mutableStateOf<String?>(null) }
	var playlistCreateDialogShown by rememberSaveable { mutableStateOf(false) }

	val isLoggedIn by SessionManager.isLoggedIn.collectAsStateWithLifecycle()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	var selectedTab by rememberSaveable { mutableStateOf(LibraryScreenTab.Home) }

	val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
	val scaleInSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

	LaunchedEffect(loginState is LoginState.Success) {
		albumsViewModel.refreshAlbums(false)
		playlistsViewModel.refreshPlaylists(false)
		artistsViewModel.refreshArtists(false)
		genresViewModel.refreshGenres(false)
		songsViewModel.refreshSongs(false)
	}

	Scaffold(
		topBar = {
			Column {
				RootTopBar({ Text(stringResource(Res.string.title_library)) }, scrollBehavior)
				LibraryScreenTabRow(
					selectedTab = selectedTab,
					onSelectTab = { selectedTab = it }
				)
			}
		},
		floatingActionButton = {
			if (selectedTab == LibraryScreenTab.Playlists && isLoggedIn) {
				AnimatedContent(
					!playlistsViewModel.gridState.lastScrolledForward
						|| Settings.shared.bottomBarCollapseMode == BottomBarCollapseMode.Never,
					transitionSpec = {
						val transformOrigin = TransformOrigin(0f, 1f)
						(slideInHorizontally(slideSpec) { it / 2 }
							+ scaleIn(scaleInSpec, transformOrigin = transformOrigin)
							+ slideInVertically(slideSpec) { it / 2 })
							.togetherWith(slideOutHorizontally(slideSpec) { it / 2 }
								+ scaleOut(transformOrigin = transformOrigin)
								+ slideOutVertically(slideSpec) { it / 2 })
							.using(SizeTransform(clip = false))
					}
				) { notScrolled ->
					if (notScrolled) {
						MediumFloatingActionButton(
							shape = MaterialTheme.shapes.large,
							containerColor = MaterialTheme.colorScheme.primary,
							onClick = {
								ctx.clickSound()
								playlistCreateDialogShown = true
							}
						) {
							Icon(
								imageVector = Icons.Outlined.Add,
								contentDescription = stringResource(Res.string.title_create_playlist),
								modifier = Modifier.size(26.dp)
							)
						}
					}
				}
			}
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			RootBottomBar(scrolled = scrollManager.isTriggered)
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = albumsState is UiState.Loading
				|| playlistsState is UiState.Loading
				|| artistsState is UiState.Loading
				|| genresState is UiState.Loading
				|| songsState is UiState.Loading,
			onRefresh = {
				if (!isLoggedIn) return@PullToRefreshBox
				albumsViewModel.refreshAlbums(true)
				playlistsViewModel.refreshPlaylists(true)
				artistsViewModel.refreshArtists(true)
				genresViewModel.refreshGenres(true)
				songsViewModel.refreshSongs(true)
			}
		) {
			if (!isLoggedIn) {
				Text(
					stringResource(Res.string.info_needs_log_in),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				return@PullToRefreshBox
			}
			when (selectedTab) {
				LibraryScreenTab.Home -> LibraryScreenHomeTab(
					scrollBehavior = scrollBehavior,
					innerPadding = innerPadding,
					onSetShareId = { shareId = it },

					albumsState = albumsState,
					selectedAlbum = selectedAlbum,
					selectedAlbumIsStarred = selectedAlbumIsStarred,
					onSelectAlbum = { albumsViewModel.selectAlbum(it) },
					onClearAlbumSelection = { albumsViewModel.clearSelection() },
					onStarSelectedAlbum = { albumsViewModel.starAlbum(it) },

					artistsState = artistsState,
					selectedArtist = selectedArtist,
					selectedArtistIsStarred = selectedArtistIsStarred,
					onSelectArtist = { artistsViewModel.selectArtist(it) },
					onClearArtistSelection = { artistsViewModel.clearSelection() },
					onStarSelectedArtist = { artistsViewModel.starArtist(it) },

					playlistsState = playlistsState,

					genresState = genresState
				)

				LibraryScreenTab.Albums -> LibraryScreenAlbumsTab(
					scrollBehavior = scrollBehavior,
					innerPadding = innerPadding,
					albumsState = albumsState,
					selectedAlbum = selectedAlbum,
					selectedAlbumIsStarred = selectedAlbumIsStarred,
					albumsListType = albumsListType,
					onSelectAlbum = { albumsViewModel.selectAlbum(it) },
					onClearAlbumSelection = { albumsViewModel.clearSelection() },
					onStarSelectedAlbum = { albumsViewModel.starAlbum(it) },
					onSetListType = {
						albumsViewModel.setListType(it)
						albumsViewModel.refreshAlbums(false)
					},
					onSetShareId = { shareId = it }
				)

				LibraryScreenTab.Artists -> LibraryScreenArtistsTab(
					scrollBehavior = scrollBehavior,
					innerPadding = innerPadding,
					artistsState = artistsState,
					selectedArtist = selectedArtist,
					selectedArtistIsStarred = selectedArtistIsStarred,
					artistsListType = artistsListType,
					gridState = artistsViewModel.gridState,
					onSelectArtist = { artistsViewModel.selectArtist(it) },
					onClearArtistSelection = { artistsViewModel.clearSelection() },
					onStarSelectedArtist = { artistsViewModel.starArtist(it) },
					onSetListType = {
						artistsViewModel.setListType(it)
						artistsViewModel.refreshArtists(false)
					}
				)

				LibraryScreenTab.Playlists -> LibraryScreenPlaylistsTab(
					scrollBehavior = scrollBehavior,
					innerPadding = innerPadding,
					playlistsState = playlistsState,
					selectedPlaylist = selectedPlaylist,
					playlistsListType = playlistsListType,
					gridState = playlistsViewModel.gridState,
					onSelectPlaylist = { playlistsViewModel.selectPlaylist(it) },
					onClearPlaylistSelection = { playlistsViewModel.clearSelection() },
					onSetListType = { playlistsViewModel.setSortMode(it) },
					onSetShareId = { shareId = it },
					onSetPlaylistDeletionId = { playlistDeletionId = it }
				)

				LibraryScreenTab.Songs -> LibraryScreenSongsTab(
					scrollBehavior = scrollBehavior,
					innerPadding = innerPadding,
					songsState = songsState,
					selectedSong = selectedSong,
					selectedSongIsStarred = selectedSongIsStarred,
					songsListType = songsListType,
					onSelectSong = { songsViewModel.selectSong(it) },
					onClearSongSelection = { songsViewModel.clearSelection() },
					onStarSelectedSong = { songsViewModel.starSong(it) },
					onSetListType = {
						songsViewModel.setListType(it)
						songsViewModel.refreshSongs(false)
					},
					onSetShareId = { shareId = it }
				)

				LibraryScreenTab.Genres -> LibraryScreenGenresTab(
					scrollBehavior = scrollBehavior,
					innerPadding = innerPadding,
					genresState = genresState
				)
			}
		}
	}

	val flattenedErrors = listOf(
		(albumsState as? UiState.Error)?.error,
		(playlistsState as? UiState.Error)?.error,
		(artistsState as? UiState.Error)?.error,
		(genresState as? UiState.Error)?.error,
		(songsState as? UiState.Error)?.error
	).mapNotNull { it?.stackTraceToString() }.takeIf { it.isNotEmpty() }?.joinToString("\n\n")

	ErrorSnackbar(
		error = flattenedErrors?.let { Error(it) },
		onClearError = {
			albumsViewModel.clearError()
			playlistsViewModel.clearError()
			artistsViewModel.clearError()
			genresViewModel.clearError()
			songsViewModel.clearError()
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
		id = playlistDeletionId,
		onIdClear = { playlistDeletionId = null },
		onRefresh = { playlistsViewModel.refreshPlaylists(false) }
	)

	if (playlistCreateDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistCreateDialog(
			onDismissRequest = { playlistCreateDialogShown = false },
			onRefresh = { playlistsViewModel.refreshPlaylists(true) }
		)
	}
}

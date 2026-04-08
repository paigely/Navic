package paige.navic.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.title_library
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.ui.screens.genre.viewmodels.GenreListViewModel
import paige.navic.ui.screens.library.components.LibraryScreenContent
import paige.navic.ui.screens.playlist.dialogs.PlaylistCreateDialog
import paige.navic.ui.screens.playlist.viewmodels.PlaylistListViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.ui.viewmodels.LoginViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.LoginState
import paige.navic.utils.UiState
import kotlin.time.Duration

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

	val playlistsViewModel = koinViewModel<PlaylistListViewModel>()
	val playlistsState by playlistsViewModel.playlistsState.collectAsStateWithLifecycle()
	val selectedPlaylist by playlistsViewModel.selectedPlaylist.collectAsStateWithLifecycle()

	val artistsViewModel = koinViewModel<ArtistListViewModel>()
	val artistsState by artistsViewModel.artistsState.collectAsStateWithLifecycle()
	val selectedArtist by artistsViewModel.selectedArtist.collectAsStateWithLifecycle()
	val selectedArtistIsStarred by artistsViewModel.starred.collectAsStateWithLifecycle()

	val genresViewModel = koinViewModel<GenreListViewModel>()
	val genresState by genresViewModel.genresState.collectAsStateWithLifecycle()

	val loginViewModel = koinViewModel<LoginViewModel>()
	val loginState by loginViewModel.loginState.collectAsStateWithLifecycle()

	var shareId by rememberSaveable { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var playlistDeletionId by rememberSaveable { mutableStateOf<String?>(null) }
	var playlistCreateDialogShown by rememberSaveable { mutableStateOf(false) }

	val isLoggedIn by SessionManager.isLoggedIn.collectAsStateWithLifecycle()
	val isOnline by albumsViewModel.isOnline.collectAsStateWithLifecycle()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	LaunchedEffect(loginState is LoginState.Success) {
		albumsViewModel.refreshAlbums(false)
		playlistsViewModel.refreshPlaylists(false)
		artistsViewModel.refreshArtists(false)
		genresViewModel.refreshGenres(false)
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
			isRefreshing = albumsState is UiState.Loading
				|| playlistsState is UiState.Loading
				|| artistsState is UiState.Loading
				|| genresState is UiState.Loading,
			onRefresh = {
				if (!isLoggedIn) return@PullToRefreshBox
				albumsViewModel.refreshAlbums(true)
				playlistsViewModel.refreshPlaylists(true)
				artistsViewModel.refreshArtists(true)
				genresViewModel.refreshGenres(true)
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
			LibraryScreenContent(
				scrollBehavior = scrollBehavior,
				innerPadding = innerPadding,
				onSetShareId = { shareId = it },
				isOnline = isOnline,

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
				selectedPlaylist = selectedPlaylist,
				onSelectPlaylist = { playlistsViewModel.selectPlaylist(it) },
				onClearPlaylistSelection = { playlistsViewModel.clearSelection() },
				onDeletePlaylist = { playlistDeletionId = it },

				genresState = genresState
			)
		}
	}

	val flattenedErrors = listOf(
		(albumsState as? UiState.Error)?.error,
		(playlistsState as? UiState.Error)?.error,
		(artistsState as? UiState.Error)?.error,
		(genresState as? UiState.Error)?.error
	).mapNotNull { it?.stackTraceToString() }.takeIf { it.isNotEmpty() }?.joinToString("\n\n")

	ErrorSnackbar(
		error = flattenedErrors?.let { Error(it) },
		onClearError = {
			albumsViewModel.clearError()
			playlistsViewModel.clearError()
			artistsViewModel.clearError()
			genresViewModel.clearError()
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

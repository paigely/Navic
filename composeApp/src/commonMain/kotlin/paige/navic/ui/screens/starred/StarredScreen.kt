package paige.navic.ui.screens.starred

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_starred
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.models.DomainSongListType
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.dialogs.QueueDuplicateDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.ui.screens.song.viewmodels.SongListViewModel
import paige.navic.ui.screens.starred.components.StarredScreenContent
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StarredScreen() {
	val songsViewModel = koinViewModel<SongListViewModel>(
		key = "starredSongs",
		parameters = { parametersOf(DomainSongListType.Starred) }
	)
	val songsState by songsViewModel.songsState.collectAsStateWithLifecycle()
	val selectedSong by songsViewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by songsViewModel.starred.collectAsStateWithLifecycle()
	val selectedSongRating by songsViewModel.selectedSongRating.collectAsStateWithLifecycle()
	val allDownloads by songsViewModel.allDownloads.collectAsStateWithLifecycle()

	val albumsViewModel = koinViewModel<AlbumListViewModel>(
		key = "starredAlbums",
		parameters = { parametersOf(DomainAlbumListType.Starred) }
	)
	val albumsState by albumsViewModel.albumsState.collectAsStateWithLifecycle()
	val selectedAlbum by albumsViewModel.selectedAlbum.collectAsStateWithLifecycle()
	val selectedAlbumIsStarred by albumsViewModel.starred.collectAsStateWithLifecycle()
	val selectedAlbumRating by albumsViewModel.rating.collectAsStateWithLifecycle()

	val artistsViewModel = koinViewModel<ArtistListViewModel>(
		key = "starredArtists",
		parameters = { parametersOf(DomainArtistListType.Starred) }
	)
	val artistsState by artistsViewModel.artistsState.collectAsStateWithLifecycle()
	val selectedArtist by artistsViewModel.selectedArtist.collectAsStateWithLifecycle()
	val selectedArtistAlbums by artistsViewModel.selectedArtistAlbums.collectAsStateWithLifecycle()
	val selectedArtistIsStarred by artistsViewModel.starred.collectAsStateWithLifecycle()

	var shareId by rememberSaveable { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val player = koinInject<MediaPlayerViewModel>()

	var songToQueue by remember { mutableStateOf<DomainSong?>(null) }

	val isOnline by songsViewModel.isOnline.collectAsStateWithLifecycle()

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_starred)) }) },
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		val isAnythingLoading = albumsState is UiState.Loading ||
			artistsState is UiState.Loading || 
			songsState is UiState.Loading
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = !isAnythingLoading,
			onRefresh = {
				albumsViewModel.refreshAlbums(true)
				artistsViewModel.refreshArtists(true)
				songsViewModel.refreshSongs(true)
			},
			key = listOf(albumsState, artistsState, songsState)
		) {
			StarredScreenContent(
				innerPadding = innerPadding,
				onSetShareId = { shareId = it },
				isOnline = isOnline,

				songsState = songsState,
				selectedSong = selectedSong,
				allDownloads = allDownloads,
				onPlaySong = { index ->
					player.clearQueue()
					songsState.data.orEmpty().forEach {
						player.addToQueueSingle(it)
					}
					player.playAt(index)
				},
				onSelectSong = {
					songsViewModel.selectSong(it)
				},
				onClearSongSelection = { songsViewModel.clearSelection() },
				selectedSongIsStarred = selectedSongIsStarred,
				onAddSongStar = { songsViewModel.starSong(true) },
				onRemoveSongStar = { songsViewModel.starSong(false) },
				onDownloadSong = { songsViewModel.downloadSong(it) },
				onCancelDownloadSong = { song -> 
					songsViewModel.cancelDownload(song.id)
				},
				onDeleteDownloadSong = { song -> 
					songsViewModel.deleteDownload(song.id) 
				},
				onPlaySongNext = { song ->
					if (player.uiState.value.queue.any { it.id == song.id }) {
						songToQueue = song
					} else {
						player.playNextSingle(song)
					}
				},
				onAddSongToQueue = { song ->
					if (player.uiState.value.queue.any { it.id == song.id }) {
						songToQueue = song
					} else {
						player.addToQueueSingle(song)
					}
				},
				selectedSongRating = selectedSongRating,
				onSetSongRating = { songsViewModel.rateSelectedSong(it) },

				albumsState = albumsState,
				selectedAlbum = selectedAlbum,
				selectedAlbumIsStarred = selectedAlbumIsStarred,
				selectedAlbumRating = selectedAlbumRating,
				onSelectAlbum = { albumsViewModel.selectAlbum(it) },
				onClearAlbumSelection = { albumsViewModel.clearSelection() },
				onStarSelectedAlbum = { albumsViewModel.starAlbum(it) },
				onPlayAlbumNext = { if (selectedAlbum != null) player.playNext(selectedAlbum as DomainSongCollection)},
				onAddAlbumToQueue = { if (selectedAlbum != null) player.addToQueue(selectedAlbum as DomainSongCollection)},
				onRateSelectedAlbum = { albumsViewModel.setRating(it) },

				artistsState = artistsState,
				selectedArtist = selectedArtist,
				selectedArtistAlbums = selectedArtistAlbums,
				selectedArtistIsStarred = selectedArtistIsStarred,
				onSelectArtist = { artistsViewModel.selectArtist(it) },
				onClearArtistSelection = { artistsViewModel.clearSelection() },
				onStarSelectedArtist = { artistsViewModel.starArtist(it) },
				onPlayArtistNext = { if (selectedArtist != null) artistsViewModel.playArtistAlbumsNext(player)},
				onAddArtistToQueue = { if (selectedArtist != null) artistsViewModel.addArtistAlbumsToQueue(player)},
			)
		}
	}

    ShareDialog(
        id = shareId,
        onIdClear = { shareId = null },
        expiry = shareExpiry,
        onExpiryChange = { shareExpiry = it }
    )

	if (songToQueue != null) {
		QueueDuplicateDialog(
			onDismissRequest = { songToQueue = null },
			onConfirm = {
				songToQueue?.let { player.addToQueueSingle(it) }
			}
		)
	}
}

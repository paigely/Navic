package paige.navic.ui.screens.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_tracks
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainSongCollection
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainAlbum
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Note
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.screens.track.components.TrackRowDropdown
import paige.navic.ui.screens.track.components.TracksScreenFooterRow
import paige.navic.ui.screens.track.components.TracksScreenHeadingRow
import paige.navic.ui.screens.track.components.TracksScreenHeadingRowButtons
import paige.navic.ui.screens.track.components.TracksScreenTopBar
import paige.navic.ui.screens.track.components.TracksScreenTrackRow
import paige.navic.ui.screens.track.components.tracksScreenMoreByArtistRow
import paige.navic.ui.screens.track.components.tracksScreenTrackRowPlaceholder
import paige.navic.ui.screens.track.viewmodels.TrackListViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.fadeFromTop
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(
    partialTracks: DomainSongCollection,
    tab: String
) {
	val viewModel = koinViewModel<TrackListViewModel>(
		key = partialTracks.toString(),
		parameters = { parametersOf(partialTracks) }
	)

	val player = koinViewModel<MediaPlayerViewModel>()

	val tracksState by viewModel.tracksState.collectAsState()
	val selection by viewModel.selectedTrack.collectAsState()
	val selectedIndex by viewModel.selectedIndex.collectAsState()
	val isOnline by viewModel.isOnline.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val albumInfoState by viewModel.albumInfoState.collectAsState()
	val starredState by viewModel.starredState.collectAsState()
	val otherAlbums by viewModel.otherAlbums.collectAsState()
	val allDownloads by viewModel.allDownloads.collectAsState()
	val downloadStatus by viewModel.collectionDownloadStatus().collectAsState(DownloadStatus.NOT_DOWNLOADED)

	val scrolled by remember {
		derivedStateOf {
			viewModel.listState.firstVisibleItemIndex >= 1
		}
	}

	Scaffold(
		topBar = {
			TracksScreenTopBar(
				albumInfoState = albumInfoState,
				tracks = tracksState,
				scrolled = scrolled,
				onSetShareId = { shareId = it },
				isOnline = isOnline,
				onDownloadAll = { viewModel.downloadAll() },
				onCancelDownloadAll = { viewModel.cancelDownloadAll() },
				downloadStatus = downloadStatus
			)
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = contentPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = tracksState is UiState.Loading,
			onRefresh = {
				viewModel.refreshTracks()
			}
		) {
			LazyColumn(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface)
					.fillMaxSize()
					.fadeFromTop(),
				horizontalAlignment = Alignment.CenterHorizontally,
				contentPadding = contentPadding.withoutTop(),
				state = viewModel.listState
			) {
				item {
					TracksScreenHeadingRow(
						partialTracks = partialTracks,
						tab = tab,
						scrolled = scrolled
					)
				}

				val error = (tracksState as? UiState.Error)
				val tracks = (tracksState as? UiState.Success)?.data
				if (error != null) {
					item { ErrorBox(error) }
					return@LazyColumn
				}
				if (tracks == null) {
					tracksScreenTrackRowPlaceholder(partialTracks.songCount)
					return@LazyColumn
				}

				item {
					TracksScreenHeadingRowButtons(
						tracks = tracks
					)
				}

				itemsIndexed(tracks.songs) { index, track ->
					val download = allDownloads.find { it.songId == track.id }
					Box {
						TracksScreenTrackRow(
							track = track,
							index = index,
							count = tracks.songs.count(),
							onClick = {
								player.clearQueue()
								player.addToQueue(tracks)
								player.playAt(index)
							},
							onLongClick = {
								viewModel.selectTrack(track, index)
							},
							onAddToQueue = {
								player.addToQueueSingle(track)
							},
							download = download,
							isOffline = !isOnline
						)
						TrackRowDropdown(
							expanded = selection == track && selectedIndex == index,
							onDismissRequest = { viewModel.clearSelection() },
							onRemoveStar = { viewModel.unstarSelectedTrack() },
							onAddStar = { viewModel.starSelectedTrack() },
							onShare = { shareId = track.id },
							tracks = tracks,
							track = track,
							onRemoveFromPlaylist = { viewModel.removeFromPlaylist() },
							starredState = starredState,
							downloadStatus = download?.status,
							onDownload = { viewModel.downloadTrack(track) },
							onCancelDownload = { viewModel.cancelDownload(track.id) },
							onDeleteDownload = { viewModel.deleteDownload(track.id) },
							onAddToQueue = { player.addToQueueSingle(track) }
						)
					}
				}

				if (tracks.songs.isEmpty()) {
					item {
						ContentUnavailable(
							icon = Icons.Outlined.Note,
							label = stringResource(Res.string.info_no_tracks)
						)
					}
				}

				item { TracksScreenFooterRow(tracks) }

				(tracks as? DomainAlbum)?.artistName?.let { artistName ->
					tracksScreenMoreByArtistRow(
						artistName = artistName,
						artistAlbums = otherAlbums,
						tab = tab
					)
				}
			}
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null; viewModel.clearSelection() },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)
}

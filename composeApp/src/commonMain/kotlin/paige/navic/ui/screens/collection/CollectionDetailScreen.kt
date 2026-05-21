package paige.navic.ui.screens.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_songs
import navic.composeapp.generated.resources.title_disc_number
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainPlaylist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Note
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.screens.collection.components.CollectionDetailScreenFooterRow
import paige.navic.ui.screens.collection.components.CollectionDetailScreenHeadingRow
import paige.navic.ui.screens.collection.components.CollectionDetailScreenHeadingRowButtons
import paige.navic.ui.screens.collection.components.CollectionDetailScreenSongRow
import paige.navic.ui.screens.collection.components.CollectionDetailScreenSongRowDropdown
import paige.navic.ui.screens.collection.components.CollectionDetailScreenTopBar
import paige.navic.ui.screens.collection.components.collectionDetailScreenMoreByArtistRow
import paige.navic.ui.screens.collection.viewmodels.CollectionDetailViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionDetailScreen(
	collectionId: String,
	tab: String
) {
	val viewModel = koinViewModel<CollectionDetailViewModel>(
		key = collectionId,
		parameters = { parametersOf(collectionId) }
	)

	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()

	val collectionState by viewModel.collectionState.collectAsState()
	val collection = collectionState.data
	val selection by viewModel.selectedSong.collectAsState()
	val isOnline by viewModel.isOnline.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	val albumInfoState by viewModel.albumInfoState.collectAsState()
	val selectedSongIsStarred by viewModel.selectedSongIsStarred.collectAsStateWithLifecycle()
	val selectedSongRating by viewModel.selectedSongRating.collectAsStateWithLifecycle()
	val otherAlbums by viewModel.otherAlbums.collectAsState()
	val allDownloads by viewModel.allDownloads.collectAsState()
	val downloadStatus by viewModel.collectionDownloadStatus()
		.collectAsState(DownloadStatus.NOT_DOWNLOADED)

	val rating by viewModel.rating.collectAsStateWithLifecycle()

	val titleAlpha by remember {
		derivedStateOf {
			if (viewModel.listState.firstVisibleItemIndex >= 1) return@derivedStateOf 1f
			val height = viewModel.listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }?.size?.toFloat() ?: 0f
			if (height > 0f) {
				val threshold = height * 0.4f
				((viewModel.listState.firstVisibleItemScrollOffset.toFloat() - threshold) / (height - threshold)).coerceIn(0f, 1f)
			} else {
				0f
			}
		}
	}

	Scaffold(
		topBar = {
			CollectionDetailScreenTopBar(
				albumInfoState = albumInfoState,
				collection = collection,
				titleAlpha = titleAlpha,
				onSetShareId = { shareId = it },
				isOnline = isOnline,
				onDownloadAll = { viewModel.downloadAll() },
				onCancelDownloadAll = { viewModel.cancelDownloadAll() },
				onPlayNext = { if (collection != null) player.playNext(collection) },
				onAddToQueue = { if (collection != null) player.addToQueue(collection) },
				downloadStatus = downloadStatus,
				rating = if (collection !is DomainPlaylist) rating else null,
				onSetRating = if (collection !is DomainPlaylist) { { viewModel.rateAlbum(it) } } else null
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
			finished = collectionState !is UiState.Loading,
			onRefresh = { viewModel.refreshCollection(true) },
			key = collectionState
		) {
			LazyColumn(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface)
					.fillMaxSize(),
				horizontalAlignment = Alignment.CenterHorizontally,
				contentPadding = contentPadding.withoutTop(),
				state = viewModel.listState
			) {
				if (collection == null) return@LazyColumn

				item {
					CollectionDetailScreenHeadingRow(
						collection = collection,
						tab = tab,
						titleAlpha = 1f - titleAlpha
					)
				}

				item {
					CollectionDetailScreenHeadingRowButtons(
						collection = collection
					)
				}

				if (collection is DomainAlbum) {
					collection.copy(
						songs = collection.songs.sortedWith(compareBy(
							{ it.discNumber },
							{ it.trackNumber }
						))
					).let { album ->
						album.songs.groupBy {it.discNumber}.forEach { group ->
							val multipleDiscs = album.songs.last().discNumber != 1
							if (group.key != null && multipleDiscs) {
								item {
									Row(
										modifier = Modifier
											.fillMaxWidth()
											.padding(horizontal = 16.dp)
											.padding(top = if (group.key == 1) 0.dp else 12.dp, bottom = 4.dp)
											.heightIn(min = 32.dp),
										verticalAlignment = Alignment.CenterVertically
									) {
										Icon(
											imageVector = Icons.Outlined.Album,
											contentDescription = null,
											tint = MaterialTheme.colorScheme.onSurfaceVariant,
											modifier = Modifier.size(20.dp)
										)

										Spacer(modifier = Modifier.width(8.dp))

										Text(
											text = stringResource(
												Res.string.title_disc_number,
												group.key as Int
											),
											style = MaterialTheme.typography.titleMediumEmphasized,
											fontWeight = FontWeight(600),
											color = MaterialTheme.colorScheme.onSurfaceVariant
										)
									}
								}
							}
							itemsIndexed(group.value) { index, song ->
								val download = allDownloads.find { it.songId == song.id }
								Box {
									CollectionDetailScreenSongRow(
										song = song,
										index = index,
										count = group.value.count(),
										isPlaylist = false,
										onClick = {
											if (playerState.currentSong?.id != song.id) {
												player.clearQueue()
												player.addToQueue(album)
												player.playAt(
													if (group.key == null || !multipleDiscs)
														index
													else 
														album.songs.indexOfFirst { it.id == song.id }
												)
											} else {
												player.togglePlay()
											}
										},
										onLongClick = {
											viewModel.selectSong(song)
										},
										onPlayNext = { 
											player.playNextSingle(song) 
										},
										onAddToQueue = {
											player.addToQueueSingle(song)
										},
										download = download,
										isOffline = !isOnline
									)
									CollectionDetailScreenSongRowDropdown(
										expanded = selection == song,
										onDismissRequest = { viewModel.clearSelection() },
										onRemoveStar = { viewModel.unstarSelectedSong() },
										onAddStar = { viewModel.starSelectedSong() },
										onShare = { shareId = song.id },
										collection = collection,
										song = song,
										onRemoveFromPlaylist = { viewModel.removeFromPlaylist() },
										starred = selectedSongIsStarred,
										downloadStatus = download?.status,
										onDownload = { viewModel.downloadSong(song) },
										onCancelDownload = { viewModel.cancelDownload(song.id) },
										onDeleteDownload = { viewModel.deleteDownload(song.id) },
										onPlayNext = { player.playNextSingle(song) },
										onAddToQueue = { player.addToQueueSingle(song) },
										isOnline = isOnline,
										rating = selectedSongRating,
										onSetRating = { viewModel.rateSelectedSong(it) }
									)
								}
							}
						}
					}
				} else {
					itemsIndexed(collection.songs) { index, song ->
						val download = allDownloads.find { it.songId == song.id }
						Box {
							CollectionDetailScreenSongRow(
								song = song,
								index = index,
								count = collection.songs.count(),
								isPlaylist = true,
								onClick = {
									if (playerState.currentSong?.id != song.id) {
										player.clearQueue()
										player.addToQueue(collection)
										player.playAt(index)
									} else {
										player.togglePlay()
									}
								},
								onLongClick = {
									viewModel.selectSong(song)
								},
								onPlayNext = { 
									player.playNextSingle(song) 
								},
								onAddToQueue = {
									player.addToQueueSingle(song)
								},
								download = download,
								isOffline = !isOnline
							)
							CollectionDetailScreenSongRowDropdown(
								expanded = selection == song,
								onDismissRequest = { viewModel.clearSelection() },
								onRemoveStar = { viewModel.unstarSelectedSong() },
								onAddStar = { viewModel.starSelectedSong() },
								onShare = { shareId = song.id },
								collection = collection,
								song = song,
								onRemoveFromPlaylist = { viewModel.removeFromPlaylist() },
								starred = selectedSongIsStarred,
								downloadStatus = download?.status,
								onDownload = { viewModel.downloadSong(song) },
								onCancelDownload = { viewModel.cancelDownload(song.id) },
								onDeleteDownload = { viewModel.deleteDownload(song.id) },
								onPlayNext = { player.playNextSingle(song) },
								onAddToQueue = { player.addToQueueSingle(song) },
								isOnline = isOnline,
								rating = selectedSongRating,
								onSetRating = { viewModel.rateSelectedSong(it) }
							)
						}
					}
				}

				if (collection.songs.isEmpty()) {
					item {
						ContentUnavailable(
							icon = Icons.Outlined.Note,
							label = stringResource(Res.string.info_no_songs)
						)
					}
				}

				item { CollectionDetailScreenFooterRow(collection) }

				(collection as? DomainAlbum)?.artistName?.let { artistName ->
					collectionDetailScreenMoreByArtistRow(
						artistName = artistName,
						artistAlbums = otherAlbums,
						tab = tab
					)
				}
			}
		}
	}

	ErrorSnackbar(
		error = (collectionState as? UiState.Error)?.error,
		onClearError = { viewModel.clearError() }
	)

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null; viewModel.clearSelection() },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)
}

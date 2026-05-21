package paige.navic.ui.screens.artist

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.info_bulk_download_warning
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_bulk_download
import navic.composeapp.generated.resources.title_similar_artists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.managers.DownloadManager
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.SongRow
import paige.navic.ui.components.dialogs.BulkDownloadDialog
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.ui.components.layouts.PagedArtCarousel
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.screens.artist.components.ArtistActionButtons
import paige.navic.ui.screens.artist.components.ArtistDetailScreenHeading
import paige.navic.ui.screens.artist.components.ArtistDetailScreenTopBar
import paige.navic.ui.screens.artist.viewmodels.ArtistDetailViewModel
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistDetailScreen(
	artistId: String
) {
	val viewModel = koinViewModel<ArtistDetailViewModel>(
		key = artistId,
		parameters = { parametersOf(artistId) }
	)
	val artistStateFlow by viewModel.artistState.collectAsStateWithLifecycle()
	val starred by viewModel.starred.collectAsStateWithLifecycle()
	val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by viewModel.selectedSongIsStarred.collectAsStateWithLifecycle()
	val selectedSongRating by viewModel.selectedSongRating.collectAsStateWithLifecycle()
	val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
	val selectedAlbumIsStarred by viewModel.selectedAlbumIsStarred.collectAsStateWithLifecycle()
	val selectedAlbumRating by viewModel.selectedAlbumRating.collectAsStateWithLifecycle()
	val allDownloads by viewModel.allDownloads.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsState(true)

	val pagedAlbums = viewModel.pagedAlbums.collectAsLazyPagingItems()

	val downloadManager = koinInject<DownloadManager>()
	val collectionDownloadStatus by viewModel.collectionDownloadStatus()
		.collectAsState(DownloadStatus.NOT_DOWNLOADED)

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var bulkDownloadDialogShown by remember { mutableStateOf(false) }

	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()

	val backStack = LocalNavStack.current
	val scope = rememberCoroutineScope()

	val gridState = rememberLazyGridState()

	Scaffold(
		topBar = {
			ArtistDetailScreenTopBar(
				scrolled = viewModel.scrollState.value > 0,
				artistState = artistStateFlow,
				starred = starred,
				onSetStarred = { viewModel.starArtist(it) }
			)
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(MaterialTheme.colorScheme.background)
		) {
			when (val state = artistStateFlow) {
				is UiState.Loading -> Box(Modifier.fillMaxSize()) {
					ContainedLoadingIndicator(Modifier.size(80.dp).align(Alignment.Center))
				}

				is UiState.Success -> {
					val artistData = state.data
					val scrolled = viewModel.scrollState.value > 0
					val onSeeAllClick = dropUnlessResumed {
						backStack.add(
							Screen.SongList(
								nested = true,
								artistId = artistId,
								artistName = artistData.artist.name
							)
						)
					}
					LazyColumn(
						modifier = Modifier
							.fillMaxSize()
							.nestedScroll(LocalBottomBarScrollManager.current.connection),
						state = viewModel.scrollState.toLazyListState(),
						contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding()),
						verticalArrangement = Arrangement.spacedBy(4.dp)
					) {
						item {
							ArtistDetailScreenHeading(
								artistName = artistData.artist.name,
								coverArtId = artistData.artist.coverArtId,
								subtitle = artistData.artist.biography,
								lastfm = artistData.artist.lastFmUrl,
								innerPadding = contentPadding,
								scrolled = scrolled
							)
						}

						item {
							ArtistActionButtons(
								modifier = Modifier.padding(top = 16.dp),
								onPlay = { viewModel.playArtistAlbums(player) },
								downloadStatus = collectionDownloadStatus,
								onDownload = { bulkDownloadDialogShown = true },
								onCancelDownload = {
									scope.launch {
										artistData.albums.forEach { album ->
											downloadManager.cancelCollectionDownload(album)
										}
									}
								},
								onDeleteDownload = {
									scope.launch {
										artistData.albums.forEach { album ->
											downloadManager.deleteDownloadedCollection(album)
										}
									}
								},
								playEnabled = true
							)
						}

						if (artistData.topSongs.isNotEmpty()) {
							item {
								Row(
									modifier = Modifier
										.fillMaxWidth()
										.padding(horizontal = 16.dp)
										.padding(top = 24.dp, bottom = 8.dp),
									verticalAlignment = Alignment.CenterVertically,
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Text(
										text = stringResource(Res.string.option_sort_frequent),
										style = MaterialTheme.typography.titleMedium,
										fontWeight = FontWeight.Bold
									)
									Text(
										text = stringResource(Res.string.action_see_all),
										style = MaterialTheme.typography.labelLarge,
										color = MaterialTheme.colorScheme.primary,
										modifier = Modifier
											.clickable(onClick = onSeeAllClick)
											.padding(8.dp)
									)
								}
							}

							item {
								val rowCount = remember(artistData.topSongs.size) {
									artistData.topSongs.size.coerceIn(1, 3)
								}
								val gridHeight = remember(rowCount) {
									when (rowCount) {
										1 -> 82.dp
										2 -> 164.dp
										else -> 246.dp
									}
								}

								LazyHorizontalGrid(
									rows = GridCells.Fixed(rowCount),
									state = gridState,
									flingBehavior = rememberSnapFlingBehavior(lazyGridState = gridState),
									modifier = Modifier
										.fillMaxWidth()
										.height(gridHeight)
								) {
									this.itemsIndexed(artistData.topSongs, key = { _, song -> song.id }) { index, song ->
										SongRow(
											modifier = Modifier.fillMaxWidth(),
											song = song,
											selected = selectedSong == song,
											onClick = {
												if (playerState.currentSong?.id != song.id) {
													player.clearQueue()
													artistData.topSongs.forEach { player.addToQueueSingle(it) }
													player.playAt(index)
												} else {
													player.togglePlay()
												}
											},
											onLongClick = { viewModel.selectSong(song) },
											onDismissRequest = { viewModel.clearSelection() },
											starredState = if (selectedSong == song) selectedSongIsStarred else false,
											onAddStar = { viewModel.starSelectedSong() },
											onRemoveStar = { viewModel.unstarSelectedSong() },
											onShare = { shareId = song.id },
											download = allDownloads.find { it.songId == song.id },
											onDownload = { viewModel.downloadSong(song) },
											onCancelDownload = { viewModel.cancelDownload(song.id) },
											onDeleteDownload = { viewModel.deleteDownload(song.id) },
											onPlayNext = { player.playNextSingle(song) },
											onAddToQueue = { player.addToQueueSingle(song) },
											isOnline = isOnline,
											rating = if (selectedSong == song) selectedSongRating else 0,
											onSetRating = { viewModel.rateSelectedSong(it) }
										)
									}
								}
							}
						}

						item {
							Spacer(Modifier.height(16.dp))
							PagedArtCarousel(
								title = stringResource(Res.string.title_albums),
								items = pagedAlbums
							) { album ->
								val onAlbumClick = dropUnlessResumed {
									backStack.add(
										Screen.CollectionDetail(
											collectionId = album.id,
											tab = "library"
										)
									)
								}
								ArtCarouselItem(
									coverArtId = album.coverArtId,
									title = album.name,
									subtitle = pluralStringResource(
										Res.plurals.count_songs,
										album.songCount,
										album.songCount
									),
									contentDescription = album.name,
									onSelect = { viewModel.selectAlbum(album) },
									onClick = onAlbumClick
								)
							}
						}

						if (artistData.similarArtists.isNotEmpty()) {
							item {
								Spacer(Modifier.height(16.dp))
								ArtCarousel(
									title = stringResource(Res.string.title_similar_artists),
									items = artistData.similarArtists.toImmutableList()
								) { artist ->
									val onSimilarArtistClick = dropUnlessResumed {
										backStack.add(Screen.ArtistDetail(artist.id))
									}
									ArtCarouselItem(
										coverArtId = artist.coverArtId,
										title = artist.name,
										subtitle = null,
										contentDescription = artist.name,
										onClick = onSimilarArtistClick
									)
								}
							}
						}

						item {
							Spacer(Modifier.height(32.dp))
						}
					}
				}

				is UiState.Error -> ErrorBox(
					modifier = Modifier.fillMaxSize(),
					error = state
				)
			}
		}
	}

	if (bulkDownloadDialogShown) {
		val artistData = (artistStateFlow as? UiState.Success)?.data
		if (artistData != null) {
			BulkDownloadDialog(
				onDismissRequest = { bulkDownloadDialogShown = false },
				title = stringResource(Res.string.title_bulk_download),
				message = stringResource(Res.string.info_bulk_download_warning, artistData.artist.name),
				showDialog = bulkDownloadDialogShown,
				onConfirm = {
					scope.launch {
						for (album in artistData.albums) {
							downloadManager.downloadCollection(album)
						}
					}
				}
			)
		}
	}

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)

	if (selectedAlbum != null) {
		CollectionSheet(
			onDismissRequest = { viewModel.clearAlbumSelection() },
			collection = selectedAlbum!!,
			starred = selectedAlbumIsStarred,
			onSetStarred = { viewModel.starAlbum(it) },
			onShare = { shareId = selectedAlbum?.id },
			onPlayNext = { player.playNext(selectedAlbum!!) },
			onAddToQueue = { player.addToQueue(selectedAlbum!!) },
			rating = selectedAlbumRating,
			onSetRating = { viewModel.rateSelectedAlbum(it) }
		)
	}

	if (shareId != null && (artistStateFlow as? UiState.Success)?.data?.topSongs?.any { it.id == shareId } == false && selectedAlbum?.id != shareId) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = persistentListOf(),
			onDismissRequest = { shareId = null }
		)
	}
}

fun ScrollState.toLazyListState(): LazyListState {
	return LazyListState(
		firstVisibleItemIndex = 0,
		firstVisibleItemScrollOffset = this.value
	)
}

fun truncateText(text: String, maxLength: Int): String {
	if (text.length <= maxLength) return text
	return text.substring(0, maxLength).substringBeforeLast(" ") + "..."
}

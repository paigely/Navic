package paige.navic.ui.screens.artist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import navic.composeapp.generated.resources.count_albums
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
import paige.navic.LocalCtx
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
import paige.navic.ui.components.layouts.ArtGridItem
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
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()

	val selection by viewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by viewModel.selectedSongIsStarred.collectAsStateWithLifecycle()
	val selectedSongRating by viewModel.selectedSongRating.collectAsStateWithLifecycle()

	val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
	val selectedAlbumIsStarred by viewModel.selectedAlbumIsStarred.collectAsStateWithLifecycle()
	val selectedAlbumRating by viewModel.selectedAlbumRating.collectAsStateWithLifecycle()

	val downloadManager = koinInject<DownloadManager>()
	val density = LocalDensity.current
	val backStack = LocalNavStack.current
	val layoutDirection = LocalLayoutDirection.current
	val artistState by viewModel.artistState.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
	val allDownloads by viewModel.allDownloads.collectAsStateWithLifecycle()
	val downloadStatus by viewModel.collectionDownloadStatus()
		.collectAsState(DownloadStatus.NOT_DOWNLOADED)
	val scope = rememberCoroutineScope()

	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

	val scrolled by remember {
		derivedStateOf {
			with(density) { viewModel.scrollState.value.toDp() } >= 200.dp
		}
	}

	var showDownloadDialog by remember { mutableStateOf(false) }

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }

	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			ArtistDetailScreenTopBar(
				scrolled = scrolled,
				artistState = artistState
			)
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
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
			modifier = Modifier.fillMaxSize()
		) {
			when (it) {
				is UiState.Error -> Box(Modifier.fillMaxSize().padding(contentPadding)) {
					ErrorBox(it)
				}

				is UiState.Loading -> Box(Modifier.fillMaxSize()) {
					ContainedLoadingIndicator(Modifier.size(80.dp).align(Alignment.Center))
				}

				is UiState.Success -> {
					val state = it.data
					BulkDownloadDialog(
						title = stringResource(Res.string.title_bulk_download),
						message = stringResource(Res.string.info_bulk_download_warning, state.artist.name),
						showDialog = showDownloadDialog,
						onDismissRequest = { showDownloadDialog = false },
						onConfirm = {
							scope.launch {
								state.albums.forEach { album ->
									downloadManager.downloadCollection(album)
								}
							}
						}
					)
					Column(
						modifier = Modifier
							.fillMaxSize()
							.verticalScroll(viewModel.scrollState),
						verticalArrangement = Arrangement.spacedBy(12.dp),
						horizontalAlignment = Alignment.CenterHorizontally
					) {
						ArtistDetailScreenHeading(
							artistName = state.artist.name,
							coverArtId = state.artist.coverArtId,
							subtitle = state.artist.biography,
							lastfm = state.artist.lastFmUrl,
							innerPadding = contentPadding,
							scrolled = scrolled
						)
						ArtistActionButtons(
							onPlay = { viewModel.playArtistAlbums(player) },
							onDownload = {
								showDownloadDialog = true
							},
							onCancelDownload = {
								state.albums.forEach { album ->
									downloadManager.cancelCollectionDownload(album)
								}
							},
							onDeleteDownload = {
								state.albums.forEach { album ->
									downloadManager.deleteDownloadedCollection(album)
								}
							},
							downloadStatus = downloadStatus,
							playEnabled = state.albums.isNotEmpty(),
							modifier = Modifier.padding(top = 8.dp),
							isOnline = isOnline
						)
						Column(
							modifier = Modifier
								.fillMaxWidth()
								.padding(
									start = contentPadding.calculateStartPadding(
										layoutDirection
									)
								)
								.padding(
									end = contentPadding.calculateEndPadding(
										layoutDirection
									)
								),
							verticalArrangement = Arrangement.spacedBy(12.dp),
							horizontalAlignment = Alignment.CenterHorizontally
						) {
							state.topSongs.takeIf { state.topSongs.isNotEmpty() }
								?.let { songs ->
									Row(
										modifier = Modifier
											.heightIn(min = 32.dp)
											.padding(top = 8.dp)
											.padding(horizontal = 16.dp)
											.fillMaxWidth(),
										verticalAlignment = Alignment.CenterVertically,
										horizontalArrangement = Arrangement.SpaceBetween
									) {
										Text(
											stringResource(Res.string.option_sort_frequent),
											style = MaterialTheme.typography.titleMediumEmphasized,
											fontWeight = FontWeight(600)
										)
										Text(
											stringResource(Res.string.action_see_all),
											style = MaterialTheme.typography.labelLarge,
											color = MaterialTheme.colorScheme.primary,
											modifier = Modifier.clickable {
												ctx.clickSound()
												backStack.add(
													Screen.SongList(
														nested = true,
														artistId = state.artist.id,
														artistName = state.artist.name
													)
												)
											}
										)
									}
									LazyHorizontalGrid(
										rows = GridCells.Fixed(3),
										modifier = Modifier.fillMaxWidth().height(250.dp)
									) {
										itemsIndexed(songs) { index, song ->
											val download = allDownloads.find { it.songId == song.id }
											SongRow(
												modifier = Modifier.weight(1f),
												song = song,
												selected = selection == song,
												onClick = {
													if (playerState.currentSong?.id != song.id) {
														player.clearQueue()
														songs.forEach { player.addToQueueSingle(it) }
														player.playAt(index)
													} else {
														player.togglePlay()
													}
												},
												onLongClick = {
													viewModel.selectSong(song)
												},
												onDismissRequest = { viewModel.clearSelection() },
												starredState = selectedSongIsStarred,
												onAddStar = { viewModel.starSelectedSong() },
												onRemoveStar = { viewModel.unstarSelectedSong() },
												download = download,
												onDownload = { viewModel.downloadSong(song) },
												onCancelDownload = { viewModel.cancelDownload(song.id) },
												onDeleteDownload = { viewModel.deleteDownload(song.id) },
												onPlayNext = { player.playNextSingle(song) },
												onAddToQueue = { player.addToQueueSingle(song) },
												onShare = { shareId = song.id },
												isOnline = isOnline,
												rating = selectedSongRating,
												onSetRating = { viewModel.rateSelectedSong(it) }
											)
										}
									}
								}
							ArtCarousel(
								stringResource(Res.string.title_albums),
								state.albums.sortedByDescending { album -> album.playCount }
									.toImmutableList()
							) { album ->
								ArtCarouselItem(
									coverArtId = album.coverArtId, 
									title = album.name, 
									contentDescription = null,
									onSelect = { viewModel.selectAlbum(album) }
								) {
									backStack.add(Screen.CollectionDetail(album.id, "artist"))
								}
								if (selectedAlbum == album) {
									CollectionSheet(
										onDismissRequest = { viewModel.clearAlbumSelection() },
										collection = album,
										starred = selectedAlbumIsStarred,
										onShare = { shareId = album.id },
										onPlayNext = { player.playNext(album) },
										onAddToQueue = { player.addToQueue(album) },
										onSetStarred = { viewModel.starAlbum(!selectedAlbumIsStarred) },
										onAddAllToPlaylist = { playlistDialogShown = true },
										isOnline = isOnline,
										rating = selectedAlbumRating,
										onSetRating = { viewModel.rateSelectedAlbum(it) }
									)
								}
							}
							if (state.similarArtists.isEmpty()) return@Column
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
								items(state.similarArtists) { artist ->
									ArtGridItem(
										modifier = Modifier.width(150.dp),
										onClick = {
											ctx.clickSound()
											backStack.add(Screen.ArtistDetail(artist.id))
										},
										coverArtId = artist.coverArtId,
										title = artist.name,
										subtitle = pluralStringResource(
											Res.plurals.count_albums,
											artist.albumCount,
											artist.albumCount
										),
										id = artist.id,
										tab = "artist"
									)
								}
							}
						}
						Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
					}
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

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = selectedAlbum?.songs.orEmpty().toPersistentList(),
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}

fun truncateText(text: String, limit: Int): String {
	return if (text.length > limit) {
		text.take(limit) + "..."
	} else {
		text
	}
}

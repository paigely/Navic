package paige.navic.ui.screens.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.insert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_queue
import navic.composeapp.generated.resources.action_remove_from_history
import navic.composeapp.generated.resources.action_search_history
import navic.composeapp.generated.resources.info_not_available_offline
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_all
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.History
import paige.navic.icons.outlined.Offline
import paige.navic.icons.outlined.Queue
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.dialogs.QueueDuplicateDialog
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.components.layouts.horizontalSection
import paige.navic.ui.components.sheets.SongSheet
import paige.navic.ui.screens.album.components.AlbumListScreenItem
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.artist.ArtistsScreenItem
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.ui.screens.search.components.SearchScreenChips
import paige.navic.ui.screens.search.components.SearchScreenTopBar
import paige.navic.ui.screens.search.viewmodels.SearchViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState

enum class SearchCategory(val res: StringResource) {
	ALL(Res.string.title_all),
	SONGS(Res.string.title_songs),
	ALBUMS(Res.string.title_albums),
	ARTISTS(Res.string.title_artists)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
	nested: Boolean
) {
	val viewModel = koinViewModel<SearchViewModel>()
	val selectedSong by viewModel.selectedSong.collectAsStateWithLifecycle()
	val selectedSongIsStarred by viewModel.selectedSongIsStarred.collectAsStateWithLifecycle()
	val selectedSongRating by viewModel.selectedSongRating.collectAsStateWithLifecycle()

	val artistListViewModel = koinViewModel<ArtistListViewModel>()
	val artistListSelection by artistListViewModel.selectedArtist.collectAsState()
	val artistListStarred by artistListViewModel.starred.collectAsState()

	val albumListViewModel = koinViewModel<AlbumListViewModel> {
		parametersOf(DomainAlbumListType.AlphabeticalByName)
	}
	val albumListSelection by albumListViewModel.selectedAlbum.collectAsState()
	val albumListStarred by albumListViewModel.starred.collectAsState()
	val selectedAlbumRating by albumListViewModel.rating.collectAsStateWithLifecycle()

	val query = viewModel.searchQuery
	val state by viewModel.searchState.collectAsState()
	val searchHistory by viewModel.searchHistory.collectAsState(initial = emptyList())
	val isOnline by viewModel.isOnline.collectAsState()
	val downloadedSongs by viewModel.downloadedSongs.collectAsState()

	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val backStack = LocalNavStack.current

	var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }
	var songToQueue by remember { mutableStateOf<DomainSong?>(null) }

	Scaffold(
		topBar = {
			Column(
				modifier = Modifier
					.background(MaterialTheme.colorScheme.surface)
					.padding(
						TopAppBarDefaults.windowInsets.asPaddingValues()
					)
			) {
				SearchScreenTopBar(
					query = query,
					nested = nested,
					onSearch = { submittedQuery ->
						viewModel.addToSearchHistory(submittedQuery)
					}
				)
				SearchScreenChips(
					selectedCategory = selectedCategory,
					onCategorySelect = { selectedCategory = it }
				)
			}
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (!nested || Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { contentPadding ->
		AnimatedContent(
			state,
			modifier = Modifier.fillMaxSize()
		) { uiState ->
			when (uiState) {
				is UiState.Loading -> ArtGrid(contentPadding = contentPadding) { artGridPlaceholder() }
				is UiState.Error -> ErrorBox(uiState, padding = contentPadding)
				is UiState.Success -> {
					val results = uiState.data
					val showAll = selectedCategory == SearchCategory.ALL
					val albums =
						if (showAll || selectedCategory == SearchCategory.ALBUMS) results.filterIsInstance<DomainAlbum>() else emptyList()
					val artists =
						if (showAll || selectedCategory == SearchCategory.ARTISTS) results.filterIsInstance<DomainArtist>() else emptyList()
					val songs =
						if (showAll || selectedCategory == SearchCategory.SONGS) results.filterIsInstance<DomainSong>() else emptyList()

					LazyVerticalGrid(
						modifier = Modifier.fillMaxSize(),
						columns = GridCells.Fixed(2),
						contentPadding = contentPadding,
						state = viewModel.gridState,
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						if (query.text.isNotBlank()) {
							if (songs.isNotEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									Text(
										stringResource(Res.string.title_songs),
										style = MaterialTheme.typography.headlineSmall,
										modifier = Modifier.padding(
											horizontal = 16.dp,
											vertical = 8.dp
										)
									)
								}
								items(
									songs.take(10).size,
									span = { GridItemSpan(maxLineSpan) }) { index ->
									val song = songs[index]
									val isDownloaded = downloadedSongs.containsKey(song.id)
									val canPlay = isOnline || isDownloaded

									val dismissState = rememberSwipeToDismissBoxState()

									LaunchedEffect(dismissState.currentValue) {
										if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
											if (player.uiState.value.queue.any { it.id == song.id }) {
												songToQueue = song
											} else {
												player.addToQueueSingle(song)
											}
											dismissState.snapTo(SwipeToDismissBoxValue.Settled)
										}
									}

									SwipeToDismissBox(
										state = dismissState,
										enableDismissFromStartToEnd = false,
										enableDismissFromEndToStart = true,
										backgroundContent = {
											val backgroundColor by animateColorAsState(
												targetValue = when (dismissState.targetValue) {
													SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
													else -> Color.Transparent
												}
											)
											val iconColor by animateColorAsState(
												targetValue = when (dismissState.targetValue) {
													SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onPrimaryContainer
													else -> MaterialTheme.colorScheme.onSurfaceVariant
												}
											)

											Box(
												modifier = Modifier
													.fillMaxSize()
													.background(color = backgroundColor)
													.padding(horizontal = 20.dp),
												contentAlignment = Alignment.CenterEnd
											) {
												Icon(
													imageVector = Icons.Outlined.Queue,
													contentDescription = stringResource(Res.string.action_add_to_queue),
													tint = iconColor
												)
											}
										}
									) {
										ListItem(
											modifier = Modifier
												.background(MaterialTheme.colorScheme.surface),
											onClick = {
												ctx.clickSound()
												player.clearQueue()
												player.addToQueueSingle(song)
												player.playAt(0)
											},
											onLongClick = { viewModel.selectSong(song) },
											content = { Text(song.title) },
											supportingContent = {
												MarqueeText(
													"${song.albumTitle ?: ""} • ${song.artistName} • ${song.year ?: ""}"
												)
											},
											leadingContent = {
												CoverArt(
													coverArtId = song.coverArtId,
													modifier = Modifier.size(50.dp),
													shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
												)
											},
											trailingContent = {
												if (!canPlay) {
													Icon(
														Icons.Outlined.Offline,
														stringResource(Res.string.info_not_available_offline),
														modifier = Modifier.size(20.dp)
													)
												}
											}
										)
										if (selectedSong == song) {
											SongSheet(
												onDismissRequest = { viewModel.clearSelectedSong() },
												song = song,
												onPlayNext = {
													if (player.uiState.value.queue.any { it.id == song.id }) {
														songToQueue = song
													} else {
														player.playNextSingle(song)
													}
												},
												onAddToQueue = {
													if (player.uiState.value.queue.any { it.id == song.id }) {
														songToQueue = song
													} else {
														player.addToQueueSingle(song)
													}
												},
												downloadStatus = if (downloadedSongs.containsKey(
														song.id
													)
												) DownloadStatus.DOWNLOADED else null,
												onTrackInfo = {
													backStack.add(Screen.SongDetail(song.id))
												},
												onViewAlbum = song.albumId?.let { albumId ->
													{
														backStack.add(
															Screen.CollectionDetail(
																collectionId = albumId,
																tab = "search"
															)
														)
													}
												},
												starred = selectedSongIsStarred,
												onSetStarred = { viewModel.starSelectedSong(it) },
												rating = selectedSongRating,
												onSetRating = { viewModel.rateSelectedSong(it) }
											)
										}
									}
								}
							}

							horizontalSection(
								title = Res.string.title_albums,
								destination = Screen.AlbumList(true),
								state = UiState.Success(albums),
								key = { it.id },
								seeAll = false
							) { album ->
								AlbumListScreenItem(
									modifier = Modifier.animateItem(fadeInSpec = null)
										.width(150.dp),
									tab = "search",
									album = album,
									selected = album == albumListSelection,
									starred = albumListStarred,
									onSelect = { albumListViewModel.selectAlbum(album) },
									onDeselect = { albumListViewModel.clearSelection() },
									onSetStarred = { albumListViewModel.starAlbum(it) },
									onSetShareId = { },
									onPlayNext = { player.playNext(album as DomainSongCollection)},
									onAddToQueue = { player.addToQueue(album as DomainSongCollection)},
									rating = selectedAlbumRating,
									onSetRating = { albumListViewModel.setRating(it) }
								)
							}

							horizontalSection(
								title = Res.string.title_artists,
								destination = Screen.ArtistList(true),
								state = UiState.Success(artists),
								key = { it.id },
								seeAll = false
							) { artist ->
								ArtistsScreenItem(
									modifier = Modifier.animateItem(fadeInSpec = null)
										.width(150.dp),
									tab = "search",
									artist = artist,
									selected = artist == artistListSelection,
									starred = artistListStarred,
									onSelect = { artistListViewModel.selectArtist(artist) },
									onDeselect = { artistListViewModel.clearSelection() },
									onSetStarred = { artistListViewModel.starArtist(it) }
								)
							}
						} else {
							if (searchHistory.isNotEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									Text(
										text = stringResource(Res.string.action_search_history),
										style = MaterialTheme.typography.titleMedium,
										color = MaterialTheme.colorScheme.primary,
										modifier = Modifier.padding(
											horizontal = 20.dp,
											vertical = 12.dp
										)
									)
								}
								items(
									searchHistory.size,
									span = { GridItemSpan(maxLineSpan) }) { index ->
									val historyItem = searchHistory[index]
									ListItem(
										modifier = Modifier.clickable {
											ctx.clickSound()
											query.clearText()
											query.edit { insert(0, historyItem) }
										},
										headlineContent = { Text(historyItem) },
										leadingContent = {
											Icon(
												imageVector = Icons.Outlined.History,
												contentDescription = null,
												tint = MaterialTheme.colorScheme.onSurfaceVariant
											)
										},
										trailingContent = {
											IconButton(onClick = {
												ctx.clickSound()
												viewModel.removeFromSearchHistory(historyItem)
											}) {
												Icon(
													imageVector = Icons.Outlined.Close,
													contentDescription = stringResource(Res.string.action_remove_from_history),
													tint = MaterialTheme.colorScheme.onSurfaceVariant
												)
											}
										}
									)
								}
							}
						}
					}
				}
			}
		}
	}

	if (songToQueue != null) {
		QueueDuplicateDialog(
			onDismissRequest = { songToQueue = null },
			onConfirm = {
				songToQueue?.let { player.addToQueueSingle(it) }
			}
		)
	}
}

package paige.navic.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Album
import dev.zt64.subsonic.api.model.AlbumListType
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.option_sort_alphabetical_by_artist
import navic.composeapp.generated.resources.option_sort_alphabetical_by_name
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import navic.composeapp.generated.resources.title_albums
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Sort
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.SelectionDropdown
import paige.navic.ui.components.dialogs.ShareDialog
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.layouts.artGridError
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.viewmodels.AlbumsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen(
	nested: Boolean = false,
	listType: AlbumListType? = null,
	viewModel: AlbumsViewModel = viewModel(key = listType?.value) {
		AlbumsViewModel(listType)
	}
) {
	val albumsState by viewModel.albumsState.collectAsState()
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val isRefreshing by viewModel.isRefreshing.collectAsState()
	val isPaginating by viewModel.isPaginating.collectAsState()
	val actions: @Composable RowScope.() -> Unit = {
		SortButton(!nested, viewModel)
	}
	val isLoggedIn = SessionManager.isLoggedIn.collectAsState()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar(
					{ Text(stringResource(Res.string.title_albums)) },
					scrollBehavior,
					actions
				)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_albums)) }, actions)
			}
		},
		bottomBar = { RootBottomBar(scrolled = viewModel.gridState.lastScrolledForward) }
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = isRefreshing || albumsState is UiState.Loading,
			onRefresh = { viewModel.refreshAlbums() }
		) {
			if (!isLoggedIn.value) {
				Text(
					stringResource(Res.string.info_needs_log_in),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				return@PullToRefreshBox
			}
			Crossfade(albumsState) { state ->
				ArtGrid(
					modifier = if (!nested)
						Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
					else Modifier,
					state = viewModel.gridState,
					contentPadding = innerPadding.withoutTop()
				) {
					when (state) {
						is UiState.Loading -> artGridPlaceholder()
						is UiState.Error -> artGridError(state)
						is UiState.Success -> {
							items(state.data, { it.id }) { album ->
								AlbumsScreenItem(
									modifier = Modifier.animateItem(),
									album = album,
									viewModel = viewModel,
									tab = "albums",
									onSetShareId = { newShareId ->
										shareId = newShareId
									}
								)
							}
							item(span = { GridItemSpan(maxLineSpan) }) {
								LaunchedEffect(viewModel.gridState) {
									snapshotFlow { viewModel.gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
										.collect { lastVisible ->
											val totalItems = viewModel.gridState.layoutInfo.totalItemsCount
											if (lastVisible != null && lastVisible >= totalItems - 1 && !isPaginating) {
												viewModel.paginate()
											}
										}
								}
								if (isPaginating) {
									Row(horizontalArrangement = Arrangement.Center) {
										ContainedLoadingIndicator(Modifier.size(48.dp))
									}
								}
							}
						}
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
}

@Composable
private fun SortButton(
	root: Boolean,
	viewModel: AlbumsViewModel
) {
	val currentListType by viewModel.listType.collectAsState()
	val items = remember {
		listOf(
			AlbumListType.Random,
			AlbumListType.Newest,
			AlbumListType.Frequent,
			AlbumListType.Recent,
			AlbumListType.Starred,
			AlbumListType.AlphabeticalByArtist,
		)
	}
	Box {
		var expanded by remember { mutableStateOf(false) }
		if (root) {
			IconButton(onClick = {
				expanded = true
			}) {
				Icon(
					Icons.Outlined.Sort,
					contentDescription = null
				)
			}
		} else {
			TopBarButton({
				expanded = true
			}) {
				Icon(
					Icons.Outlined.Sort,
					contentDescription = null
				)
			}
		}
		SelectionDropdown(
			items = items,
			label = {
				it.label()
			},
			expanded = expanded,
			onDismissRequest = { expanded = false },
			selection = currentListType,
			onSelect = {
				viewModel.setListType(it)
				viewModel.refreshAlbums()
			}
		)
	}
}

@Composable
private fun AlbumListType.label() =
	when (this) {
		AlbumListType.Random -> stringResource(Res.string.option_sort_random)
		AlbumListType.Newest -> stringResource(Res.string.option_sort_newest)
		AlbumListType.Frequent -> stringResource(Res.string.option_sort_frequent)
		AlbumListType.Recent -> stringResource(Res.string.option_sort_recent)
		AlbumListType.AlphabeticalByName -> stringResource(Res.string.option_sort_alphabetical_by_name)
		AlbumListType.AlphabeticalByArtist -> stringResource(Res.string.option_sort_alphabetical_by_artist)
		AlbumListType.Starred -> stringResource(Res.string.option_sort_starred)
		else -> "$this"
	}

@Composable
fun AlbumsScreenItem(
	modifier: Modifier = Modifier,
	album: Album,
	tab: String,
	viewModel: AlbumsViewModel,
	onSetShareId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedAlbum.collectAsState()
	val starredState by viewModel.starredState.collectAsState()
	Box(modifier) {
		ArtGridItem(
			onClick = {
				ctx.clickSound()
				backStack.add(Screen.Tracks(album, tab))
			},
			onLongClick = { viewModel.selectAlbum(album) },
			coverArt = album.coverArtId,
			title = album.name,
			subtitle = album.artistName,
			id = album.id,
			tab = tab
		)
		Dropdown(
			expanded = selection == album,
			onDismissRequest = {
				viewModel.selectAlbum(null)
			}
		) {
			DropdownItem(
				text = { Text(stringResource(Res.string.action_share)) },
				leadingIcon = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					viewModel.selectAlbum(null)
					onSetShareId(album.id)
				},
			)
			val starred =
				(starredState as? UiState.Success)?.data
			DropdownItem(
				text = {
					Text(
						stringResource(
							if (starred == true)
								Res.string.action_remove_star
							else Res.string.action_star
						)
					)
				},
				leadingIcon = {
					Icon(if (starred == true) Icons.Filled.Star else Icons.Outlined.Star, null)
				},
				onClick = {
					viewModel.starAlbum(starred != true)
					viewModel.selectAlbum(null)
				},
				enabled = starred != null
			)
		}
	}
}
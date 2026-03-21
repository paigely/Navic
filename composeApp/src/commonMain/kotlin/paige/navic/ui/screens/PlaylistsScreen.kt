package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Playlist
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.option_sort_ascending
import navic.composeapp.generated.resources.option_sort_descending
import navic.composeapp.generated.resources.title_create_playlist
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.settings.enums.PlaylistSortMode
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarCollapseMode
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Sort
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.SelectionDropdown
import paige.navic.ui.components.common.SelectionDropdownItem
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.dialogs.ShareDialog
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.layouts.artGridError
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.ui.viewmodels.PlaylistsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistsScreen(
	nested: Boolean = false,
	viewModel: PlaylistsViewModel = viewModel { PlaylistsViewModel() }
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current

	val playlistsState by viewModel.playlistsState.collectAsState()
	val isRefreshing by viewModel.isRefreshing.collectAsState()

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val isLoggedIn by SessionManager.isLoggedIn.collectAsState()

	val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
	val scaleInSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar(
					title = { Text(stringResource(Res.string.title_playlists)) },
					scrollBehavior = scrollBehavior,
					actions = { SortButton(!nested, viewModel) }
				)
			} else {
				NestedTopBar(
					title = { Text(stringResource(Res.string.title_playlists)) },
					actions = { SortButton(!nested, viewModel) }
				)
			}
		},
		floatingActionButton = {
			if (!isLoggedIn) return@Scaffold
			AnimatedContent(
				!viewModel.gridState.lastScrolledForward
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
							if (backStack.lastOrNull() !is Screen.CreatePlaylist) {
								backStack.add(Screen.CreatePlaylist())
							}
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
		},
		bottomBar = {
			if (!nested || Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = viewModel.gridState.lastScrolledForward)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			isRefreshing = isRefreshing || playlistsState is UiState.Loading,
			onRefresh = { viewModel.refreshPlaylists() }
		) {
			if (!isLoggedIn) {
				Text(
					stringResource(Res.string.info_needs_log_in),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				return@PullToRefreshBox
			}
			Crossfade(playlistsState) { state ->
				ArtGrid(
					modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
					state = viewModel.gridState,
					contentPadding = innerPadding.withoutTop()
				) {
					when (state) {
						is UiState.Loading -> artGridPlaceholder()
						is UiState.Error -> artGridError(state)
						is UiState.Success -> {
							items(state.data, { it.id }) { playlist ->
								PlaylistsScreenItem(
									modifier = Modifier.animateItem(fadeInSpec = null),
									playlist = playlist,
									tab = "playlists",
									viewModel = viewModel,
									onSetShareId = { newShareId ->
										shareId = newShareId
									},
									onSetDeletionId = { newDeletionId ->
										deletionId = newDeletionId
									}
								)
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

	@Suppress("AssignedValueIsNeverRead")
	DeletionDialog(
		endpoint = DeletionEndpoint.PLAYLIST,
		id = deletionId,
		onIdClear = { deletionId = null }
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SortButton(
	root: Boolean,
	viewModel: PlaylistsViewModel
) {
	val items = remember { PlaylistSortMode.entries }
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
			label = { stringResource(it.displayName) },
			expanded = expanded,
			onDismissRequest = { expanded = false },
			selection = Settings.shared.playlistSortMode,
			onSelect = {
				Settings.shared.playlistSortMode = it
				viewModel.sortPlaylists()
			},
			footer = {
				SelectionDropdownItem(
					label = stringResource(Res.string.option_sort_ascending),
					selected = !Settings.shared.playlistsReversed,
					index = 5,
					onClick = {
						Settings.shared.playlistsReversed = false
						expanded = false
						viewModel.sortPlaylists()
					}
				)
				SelectionDropdownItem(
					label = stringResource(Res.string.option_sort_descending),
					selected = Settings.shared.playlistsReversed,
					index = 4,
					onClick = {
						Settings.shared.playlistsReversed = true
						expanded = false
						viewModel.sortPlaylists()
					}
				)
			}
		)
	}
}

@Composable
fun PlaylistsScreenItem(
	modifier: Modifier = Modifier,
	playlist: Playlist,
	tab: String,
	viewModel: PlaylistsViewModel,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val selection by viewModel.selectedPlaylist.collectAsState()
	Box(modifier) {
		ArtGridItem(
			onClick = {
				ctx.clickSound()
				backStack.add(Screen.Tracks(playlist, "playlists"))
			},
			onLongClick = { viewModel.selectPlaylist(playlist) },
			coverArtId = playlist.coverArtId,
			title = playlist.name,
			subtitle = buildString {
				append(
					pluralStringResource(
						Res.plurals.count_songs,
						playlist.songCount,
						playlist.songCount
					)
				)
				playlist.comment?.let {
					append("\n${playlist.comment}\n")
				}
			},
			id = playlist.id,
			tab = tab
		)
		Dropdown(
			expanded = selection == playlist,
			onDismissRequest = {
				viewModel.clearSelection()
			}
		) {
			DropdownItem(
				text = { Text(stringResource(Res.string.action_share)) },
				leadingIcon = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					onSetShareId(playlist.id)
					viewModel.clearSelection()
				},
			)
			DropdownItem(
				text = { Text(stringResource(Res.string.action_delete)) },
				leadingIcon = { Icon(Icons.Outlined.PlaylistRemove, null) },
				onClick = {
					onSetDeletionId(playlist.id)
					viewModel.clearSelection()
				}
			)
		}
	}
}
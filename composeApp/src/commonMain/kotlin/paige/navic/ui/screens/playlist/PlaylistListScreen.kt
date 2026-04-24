package paige.navic.ui.screens.playlist

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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFloatingActionButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_create_playlist
import navic.composeapp.generated.resources.title_playlists
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarCollapseMode
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.dialogs.DeletionDialog
import paige.navic.ui.components.dialogs.DeletionEndpoint
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.playlist.components.PlaylistListScreenSortButton
import paige.navic.ui.screens.playlist.components.playlistListScreenContent
import paige.navic.ui.screens.playlist.dialogs.PlaylistCreateDialog
import paige.navic.ui.screens.playlist.viewmodels.PlaylistListViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PlaylistListScreen(
	nested: Boolean = false
) {
	val viewModel = koinViewModel<PlaylistListViewModel>()
	val player = koinViewModel<MediaPlayerViewModel>()
	val playlistsState by viewModel.playlistsState.collectAsState()
	val selectedPlaylist by viewModel.selectedPlaylist.collectAsState()
	val selectedSorting by viewModel.selectedSorting.collectAsStateWithLifecycle()
	val selectedReversed by viewModel.selectedReversed.collectAsStateWithLifecycle()

	val ctx = LocalCtx.current
	val scrollManager = LocalBottomBarScrollManager.current

	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	var deletionId by remember { mutableStateOf<String?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
	val scaleInSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

	var createDialogShown by rememberSaveable { mutableStateOf(false) }

	val gridState = rememberLazyGridState()

	val actions: @Composable RowScope.() -> Unit = {
		PlaylistListScreenSortButton(
			nested = nested,
			selectedSorting = selectedSorting,
			onSetSorting = { viewModel.setSorting(it) },
			selectedReversed = selectedReversed,
			onSetReversed = { viewModel.setReversed(it) }
		)
	}

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar(
					title = { Text(stringResource(Res.string.title_playlists)) },
					scrollBehavior = scrollBehavior,
					actions = actions
				)
			} else {
				NestedTopBar(
					title = { Text(stringResource(Res.string.title_playlists)) },
					actions = actions
				)
			}
		},
		floatingActionButton = {
			AnimatedContent(
				!scrollManager.isTriggered
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
							createDialogShown = true
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
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = playlistsState !is UiState.Loading,
			onRefresh = { viewModel.refreshPlaylists(true) },
			key = playlistsState
		) {
			ArtGrid(
				modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
				state = gridState,
				contentPadding = innerPadding.withoutTop(),
				verticalArrangement = if ((playlistsState as? UiState.Success)?.data?.isEmpty() == true)
					Arrangement.Center
				else Arrangement.spacedBy(12.dp)
			) {
				playlistListScreenContent(
					state = playlistsState,
					selectedPlaylist = selectedPlaylist,
					onUpdateSelection = { viewModel.selectPlaylist(it) },
					onClearSelection = { viewModel.clearSelection() },
					onSetShareId = { newShareId ->
						shareId = newShareId
					},
					onSetDeletionId = { newDeletionId ->
						deletionId = newDeletionId
					},
					onAddToQueue = { if (selectedPlaylist != null) player.addToQueue(selectedPlaylist as DomainSongCollection)}
				)
			}
		}
	}

	ErrorSnackbar(
		error = (playlistsState as? UiState.Error)?.error,
		onClearError = { viewModel.clearError() }
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
		id = deletionId,
		onIdClear = { deletionId = null },
		onRefresh = { viewModel.refreshPlaylists(true) }
	)

	if (createDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistCreateDialog(
			onDismissRequest = { createDialogShown = false },
			onRefresh = { viewModel.refreshPlaylists(true) }
		)
	}
}

package paige.navic.ui.screens.album

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_albums
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainSongCollection
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.album.components.AlbumListScreenSortButton
import paige.navic.ui.screens.album.components.albumListScreenContent
import paige.navic.ui.screens.album.viewmodels.AlbumListViewModel
import paige.navic.ui.screens.share.dialogs.ShareDialog
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.withoutTop
import kotlin.time.Duration

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumListScreen(
	nested: Boolean = false,
	listType: DomainAlbumListType
) {
	val viewModel = koinViewModel<AlbumListViewModel>(
		key = listType.toString(),
		parameters = { parametersOf(listType) }
	)
	val player = koinViewModel<MediaPlayerViewModel>()
	val pagedAlbums = viewModel.pagedAlbums.collectAsLazyPagingItems()
	val selectedSorting by viewModel.listType.collectAsStateWithLifecycle()
	val selectedReversed by viewModel.selectedReversed.collectAsStateWithLifecycle()
	val selectedAlbum by viewModel.selectedAlbum.collectAsStateWithLifecycle()
	val starred by viewModel.starred.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
	var shareId by remember { mutableStateOf<String?>(null) }
	var shareExpiry by remember { mutableStateOf<Duration?>(null) }
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	val actions: @Composable RowScope.() -> Unit = {
		AlbumListScreenSortButton(
			nested = nested,
			selectedSorting = selectedSorting,
			onSetSorting = { viewModel.setListType(it) },
			selectedReversed = selectedReversed,
			onSetReversed = { viewModel.setReversed(it) }
		)
	}

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
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (!nested || Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		val isRefreshing = pagedAlbums.loadState.refresh is LoadState.Loading
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = !isRefreshing,
			onRefresh = { pagedAlbums.refresh() },
			key = pagedAlbums.itemSnapshotList
		) {
			ArtGrid(
				modifier = if (!nested)
					Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
				else Modifier,
				state = viewModel.gridState,
				contentPadding = innerPadding.withoutTop(),
				verticalArrangement = if (pagedAlbums.itemCount == 0 && !isRefreshing)
					Arrangement.Center
				else Arrangement.spacedBy(12.dp)
			) {
				albumListScreenContent(
					pagedAlbums = pagedAlbums,
					starred = starred,
					selectedAlbum = selectedAlbum,
					onAddToQueue = { if (selectedAlbum != null) player.addToQueue(selectedAlbum as DomainSongCollection) },
					onUpdateSelection = { viewModel.selectAlbum(it) },
					onClearSelection = { viewModel.clearSelection() },
					onSetShareId = { newShareId ->
						shareId = newShareId
					},
					onSetStarred = { viewModel.starAlbum(it) },
					isOnline = isOnline
				)
			}
		}
	}

	ErrorSnackbar(
		error = (pagedAlbums.loadState.refresh as? LoadState.Error)?.error
			?: (pagedAlbums.loadState.append as? LoadState.Error)?.error,
		onClearError = { viewModel.clearError() }
	)

	@Suppress("AssignedValueIsNeverRead")
	ShareDialog(
		id = shareId,
		onIdClear = { shareId = null },
		expiry = shareExpiry,
		onExpiryChange = { shareExpiry = it }
	)
}

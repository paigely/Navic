package paige.navic.ui.screens.artist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.count_albums
import navic.composeapp.generated.resources.title_artists
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.domain.models.DomainArtist
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.layouts.ArtGridItem
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.PullToRefreshBox
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.artist.components.ArtistListScreenContent
import paige.navic.ui.screens.artist.viewmodels.ArtistListViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtistListScreen(
	nested: Boolean = false
) {
	val viewModel = koinViewModel<ArtistListViewModel>()
	val artistsState by viewModel.artistsState.collectAsState()
	val selectedArtist by viewModel.selectedArtist.collectAsState()
	val starred by viewModel.starred.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar({ Text(stringResource(Res.string.title_artists)) }, scrollBehavior)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_artists)) })
			}
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (!nested || Settings.shared.bottomBarVisibilityMode == BottomBarVisibilityMode.AllScreens) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			finished = artistsState !is UiState.Loading,
			onRefresh = { viewModel.refreshArtists(true) },
			key = artistsState
		) {
			ArtistListScreenContent(
				state = artistsState,
				starred = starred,
				selectedArtist = selectedArtist,
				gridState = viewModel.gridState,
				scrollBehavior = scrollBehavior,
				innerPadding = innerPadding,
				nested = nested,
				onUpdateSelection = { viewModel.selectArtist(it) },
				onClearSelection = { viewModel.clearSelection() },
				onSetStarred = { viewModel.starArtist(it) }
			)
		}
	}

	ErrorSnackbar(
		error = (artistsState as? UiState.Error)?.error,
		onClearError = { viewModel.clearError() }
	)
}

@Composable
fun ArtistsScreenItem(
	modifier: Modifier = Modifier,
	tab: String,
	artist: DomainArtist,
	selected: Boolean,
	starred: Boolean,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	Box(modifier) {
		ArtGridItem(
			onClick = dropUnlessResumed {
				ctx.clickSound()
				backStack.add(Screen.ArtistDetail(artist.id))
			},
			onLongClick = onSelect,
			coverArtId = artist.coverArtId,
			title = artist.name,
			subtitle = pluralStringResource(
				Res.plurals.count_albums,
				artist.albumCount,
				artist.albumCount
			),
			id = artist.id,
			tab = tab
		)
		Dropdown(
			expanded = selected,
			onDismissRequest = onDeselect
		) {
			DropdownItem(
				text = {
					Text(
						stringResource(
							if (starred)
								Res.string.action_remove_star
							else Res.string.action_star
						)
					)
				},
				leadingIcon = {
					Icon(if (starred) Icons.Filled.Star else Icons.Outlined.Star, null)
				},
				onClick = {
					onSetStarred(!starred)
				}
			)
		}
	}
}

package paige.navic.ui.screens.genre

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_genres
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.genre.components.genreListScreenContent
import paige.navic.ui.screens.genre.viewmodels.GenreListViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun GenreListScreen(
	nested: Boolean
) {
	val viewModel = koinViewModel<GenreListViewModel>()
	val genresState by viewModel.genresState.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	var isRefreshing by remember { mutableStateOf(false) }
	val state = rememberPullToRefreshState()

	LaunchedEffect(genresState) {
		if (genresState !is UiState.Loading) {
			isRefreshing = false
		}
	}

	val onRefresh: () -> Unit = {
		isRefreshing = true
		viewModel.refreshGenres(true)
	}

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar(
					{ Text(stringResource(Res.string.title_genres)) },
					scrollBehavior
				)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_genres)) })
			}
		},
		bottomBar = {
			val scrollManager = LocalBottomBarScrollManager.current
			if (!nested) {
				RootBottomBar(scrolled = scrollManager.isTriggered)
			}
		}
	) { innerPadding ->
		PullToRefreshBox(
			modifier = Modifier
				.padding(top = innerPadding.calculateTopPadding())
				.background(MaterialTheme.colorScheme.surface),
			state = state,
			isRefreshing = isRefreshing,
			onRefresh = onRefresh,
			indicator = {
				Box(
					Modifier.align(Alignment.TopCenter).graphicsLayer {
						val scaleFraction = if (isRefreshing) 1f
						else LinearOutSlowInEasing.transform(state.distanceFraction).coerceIn(0f, 1f)
						scaleX = scaleFraction
						scaleY = scaleFraction
					}
				) {
					PullToRefreshDefaults.LoadingIndicator(state = state, isRefreshing = isRefreshing)
				}
			}
		) {
			ArtGrid(
				modifier = if (!nested)
					Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
				else Modifier,
				contentPadding = innerPadding.withoutTop(),
				state = viewModel.gridState,
				verticalArrangement = if ((genresState as? UiState.Success)?.data?.isEmpty() == true)
					Arrangement.Center
				else Arrangement.spacedBy(12.dp)
			) {
				genreListScreenContent(state = genresState)
			}
		}
	}

	ErrorSnackbar(
		error = (genresState as? UiState.Error)?.error,
		onClearError = { viewModel.clearError() }
	)
}

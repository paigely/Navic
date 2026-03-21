package paige.navic.ui.screens.genres

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_needs_log_in
import navic.composeapp.generated.resources.info_no_genres
import navic.composeapp.generated.resources.title_genres
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Genre
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.components.layouts.artGridError
import paige.navic.ui.viewmodels.GenresViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(
	nested: Boolean,
	viewModel: GenresViewModel = viewModel { GenresViewModel() }
) {
	val state by viewModel.genresState.collectAsState()
	val isRefreshing by viewModel.isRefreshing.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
	val isLoggedIn by SessionManager.isLoggedIn.collectAsState()

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
			isRefreshing = isRefreshing || state is UiState.Loading,
			onRefresh = { viewModel.refreshGenres() }
		) {
			if (!isLoggedIn) {
				Text(
					stringResource(Res.string.info_needs_log_in),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				return@PullToRefreshBox
			}
			Crossfade(state) { state ->
				ArtGrid(
					modifier = if (!nested)
						Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
					else Modifier,
					contentPadding = innerPadding.withoutTop(),
					state = viewModel.gridState,
					verticalArrangement = if ((state as? UiState.Success)?.data?.isEmpty() == true)
						Arrangement.Center
					else Arrangement.spacedBy(12.dp)
				) {
					when (state) {
						is UiState.Error -> artGridError(state)
						is UiState.Loading -> items(10) { GenreCardPlaceholder() }
						is UiState.Success -> {
							items(state.data, { it.genre.name }) { genre ->
								GenreCard(genre = genre)
							}
							if (state.data.isEmpty()) {
								item(span = { GridItemSpan(maxLineSpan) }) {
									ContentUnavailable(
										icon = Icons.Outlined.Genre,
										label = stringResource(Res.string.info_no_genres)
									)
								}
							}
						}
					}
				}
			}
		}
	}
}
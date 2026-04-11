package paige.navic.ui.screens.radio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
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
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_radios
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ErrorSnackbar
import paige.navic.ui.components.layouts.ArtGrid
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.layouts.RootTopBar
import paige.navic.ui.screens.radio.components.radioListScreenContent
import paige.navic.ui.screens.radio.viewmodels.RadioListViewModel
import paige.navic.utils.LocalBottomBarScrollManager
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioListScreen(
	nested: Boolean
) {
	val viewModel = koinViewModel<RadioListViewModel>()
	val player = koinViewModel<MediaPlayerViewModel>()
	val state by viewModel.radiosState.collectAsState()
	val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

	Scaffold(
		topBar = {
			if (!nested) {
				RootTopBar(
					{ Text(stringResource(Res.string.title_radios)) },
					scrollBehavior
				)
			} else {
				NestedTopBar({ Text(stringResource(Res.string.title_radios)) })
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
			isRefreshing = state is UiState.Loading,
			onRefresh = { viewModel.refreshRadios(true) }
		) {
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
				radioListScreenContent(
					state = state,
					onRadioClick = { radio ->
						player.playRadio(radio)
					}
				)
			}
		}
	}

	ErrorSnackbar(
		error = (state as? UiState.Error)?.error,
		onClearError = { viewModel.clearError() }
	)
}

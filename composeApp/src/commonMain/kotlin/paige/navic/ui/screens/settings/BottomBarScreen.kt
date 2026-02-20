package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_show_progress_in_bar
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.option_use_detached_bar
import navic.composeapp.generated.resources.title_bottom_app_bar
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.data.models.Settings
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.dialogs.NavtabsDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.settings.SettingSwitchRow

@Composable
fun BottomBarScreen() {
	val ctx = LocalCtx.current
	var showNavtabsDialog by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_bottom_app_bar)) },
			hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
		) },
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
			) {
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_short_navigation_bar)) },
						value = Settings.shared.useShortNavbar,
						onSetValue = { Settings.shared.useShortNavbar = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_use_detached_bar)) },
						value = Settings.shared.detachedBar,
						onSetValue = { Settings.shared.detachedBar = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_swipe_to_skip)) },
						value = Settings.shared.swipeToSkip,
						onSetValue = { Settings.shared.swipeToSkip = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_show_progress_in_bar)) },
						value = Settings.shared.showProgressInBar,
						onSetValue = { Settings.shared.showProgressInBar = it }
					)

					FormRow(
						onClick = { showNavtabsDialog = true }
					) {
						Text(stringResource(Res.string.option_navbar_tab_positions))
					}
				}
				Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
			}
		}
		NavtabsDialog(
			presented = showNavtabsDialog,
			onDismissRequest = { showNavtabsDialog = false }
		)
	}
}
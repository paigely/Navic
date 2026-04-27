package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_bottom_bar_collapse_mode
import navic.composeapp.generated.resources.option_bottom_bar_visibility_mode
import navic.composeapp.generated.resources.option_mini_player_progress_style
import navic.composeapp.generated.resources.option_mini_player_style
import navic.composeapp.generated.resources.option_navigation_bar_label_visibility
import navic.composeapp.generated.resources.option_navigation_bar_style
import navic.composeapp.generated.resources.option_navigation_bar_tabs
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.title_bottom_app_bar
import navic.composeapp.generated.resources.title_mini_player
import navic.composeapp.generated.resources.title_navigation_bar
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarCollapseMode
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.data.models.settings.enums.MiniPlayerProgressStyle
import paige.navic.data.models.settings.enums.MiniPlayerStyle
import paige.navic.data.models.settings.enums.NavigationBarLabelVisibility
import paige.navic.data.models.settings.enums.NavigationBarStyle
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSelectionRow
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.ui.screens.settings.dialogs.NavtabsDialog

@Composable
fun BottomBarScreen() {
	val ctx = LocalCtx.current
	var showNavtabsDialog by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_bottom_app_bar)) },
				hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		}
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
						title = { Text(stringResource(Res.string.option_swipe_to_skip)) },
						value = Settings.shared.swipeToSkip,
						onSetValue = { Settings.shared.swipeToSkip = it }
					)

					SettingSelectionRow(
						items = BottomBarCollapseMode.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.bottomBarCollapseMode,
						onSelect = { Settings.shared.bottomBarCollapseMode = it },
						title = { Text(stringResource(Res.string.option_bottom_bar_collapse_mode)) },
					)

					SettingSelectionRow(
						items = BottomBarVisibilityMode.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.bottomBarVisibilityMode,
						onSelect = { Settings.shared.bottomBarVisibilityMode = it },
						title = { Text(stringResource(Res.string.option_bottom_bar_visibility_mode)) },
					)
				}

				FormTitle(stringResource(Res.string.title_navigation_bar))
				Form {
					SettingSelectionRow(
						items = NavigationBarStyle.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.navigationBarStyle,
						onSelect = { Settings.shared.navigationBarStyle = it },
						title = { Text(stringResource(Res.string.option_navigation_bar_style)) },
					)

					SettingSelectionRow(
						items = NavigationBarLabelVisibility.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.navigationBarLabelVisibility,
						onSelect = { Settings.shared.navigationBarLabelVisibility = it },
						title = { Text(stringResource(Res.string.option_navigation_bar_label_visibility)) },
					)

					FormRow(
						onClick = { showNavtabsDialog = true }
					) {
						Text(stringResource(Res.string.option_navigation_bar_tabs))
						Icon(Icons.Outlined.ChevronForward, null)
					}
				}

				FormTitle(stringResource(Res.string.title_mini_player))
				Form {
					SettingSelectionRow(
						items = MiniPlayerStyle.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.miniPlayerStyle,
						onSelect = { Settings.shared.miniPlayerStyle = it },
						title = { Text(stringResource(Res.string.option_mini_player_style)) },
					)

					SettingSelectionRow(
						items = MiniPlayerProgressStyle.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.miniPlayerProgressStyle,
						onSelect = { Settings.shared.miniPlayerProgressStyle = it },
						title = { Text(stringResource(Res.string.option_mini_player_progress_style)) },
					)
				}
			}
		}
		NavtabsDialog(
			presented = showNavtabsDialog,
			onDismissRequest = { showNavtabsDialog = false }
		)
	}
}

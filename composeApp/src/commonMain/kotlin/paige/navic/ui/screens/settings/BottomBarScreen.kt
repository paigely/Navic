package paige.navic.ui.screens.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_bottom_bar_collapse_mode
import navic.composeapp.generated.resources.option_bottom_bar_visibility_mode
import navic.composeapp.generated.resources.option_navigation_bar_style
import navic.composeapp.generated.resources.option_navigation_bar_tabs
import navic.composeapp.generated.resources.option_player_bar_progress_style
import navic.composeapp.generated.resources.option_player_bar_style
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.title_bottom_app_bar
import navic.composeapp.generated.resources.title_navigation_bar
import navic.composeapp.generated.resources.title_player_bar
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.BottomBarCollapseMode
import paige.navic.data.models.settings.enums.BottomBarVisibilityMode
import paige.navic.data.models.settings.enums.NavigationBarStyle
import paige.navic.data.models.settings.enums.PlayerBarProgressStyle
import paige.navic.data.models.settings.enums.PlayerBarStyle
import paige.navic.data.models.settings.enums.ThemeMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.dialogs.NavtabsDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.RootBottomBar
import paige.navic.ui.components.settings.SettingSelectionRow
import paige.navic.ui.components.settings.SettingSwitchRow
import paige.navic.ui.theme.defaultFont
import paige.navic.utils.fadeFromTop

@Composable
fun BottomBarScreen() {
	val ctx = LocalCtx.current
	var showNavtabsDialog by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_bottom_app_bar)) },
			hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
		) }
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
					.fadeFromTop()
			) {
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_swipe_to_skip)) },
						value = Settings.shared.swipeToSkip,
						onSetValue = { Settings.shared.swipeToSkip = it }
					)

					SettingSelectionRow(
						items = BottomBarCollapseMode.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.bottomBarCollapseMode,
						onSelect = { Settings.shared.bottomBarCollapseMode = it },
						title = { Text(stringResource(Res.string.option_bottom_bar_collapse_mode)) },
					)

					SettingSelectionRow(
						items = BottomBarVisibilityMode.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.bottomBarVisibilityMode,
						onSelect = { Settings.shared.bottomBarVisibilityMode = it },
						title = { Text(stringResource(Res.string.option_bottom_bar_visibility_mode)) },
					)
				}

				FormTitle(stringResource(Res.string.title_navigation_bar))
				Form {
					SettingSelectionRow(
						items = NavigationBarStyle.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.navigationBarStyle,
						onSelect = { Settings.shared.navigationBarStyle = it },
						title = { Text(stringResource(Res.string.option_navigation_bar_style)) },
					)

					FormRow(
						onClick = { showNavtabsDialog = true }
					) {
						Text(stringResource(Res.string.option_navigation_bar_tabs))
						Icon(Icons.Outlined.ChevronForward, null)
					}
				}

				FormTitle(stringResource(Res.string.title_player_bar))
				Form {
					SettingSelectionRow(
						items = PlayerBarStyle.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.playerBarStyle,
						onSelect = { Settings.shared.playerBarStyle = it },
						title = { Text(stringResource(Res.string.option_player_bar_style)) },
					)

					SettingSelectionRow(
						items = PlayerBarProgressStyle.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.playerBarProgressStyle,
						onSelect = { Settings.shared.playerBarProgressStyle = it },
						title = { Text(stringResource(Res.string.option_player_bar_progress_style)) },
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomBarPreview() {
	val inDarkTheme = isSystemInDarkTheme()
	val isDark = remember(Settings.shared.themeMode) {
		when (Settings.shared.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
	val padding by animateDpAsState(
		if (Settings.shared.playerBarStyle == PlayerBarStyle.Detached) 12.dp else 32.dp, spec
	)
	val rounding by animateDpAsState(
		if (Settings.shared.playerBarStyle == PlayerBarStyle.Detached) 28.dp else 12.dp, spec
	)
	Column(Modifier.padding(16.dp).navigationBarsPadding()) {
		Text(
			"Preview",
			style = MaterialTheme.typography.titleSmallEmphasized,
			fontWeight = FontWeight(600),
			fontFamily = defaultFont(round = 100f),
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth()
		)
		Spacer(Modifier.height(10.dp))
		RootBottomBar(
			scrolled = false,
			shadows = false,
			modifier = Modifier
				.clip(ContinuousRoundedRectangle(rounding))
				.background(
					if (isDark)
						MaterialTheme.colorScheme.surfaceContainerLow
					else MaterialTheme.colorScheme.surfaceContainerHighest
				)
				.fillMaxWidth()
				.padding(top = padding),
			bottomBarWindowInsets = WindowInsets()
		)
	}
}

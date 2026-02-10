package paige.navic.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_enable_scrobbling
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.option_min_duration_to_scrobble
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_scrobble_percentage
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_show_progress_in_bar
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.option_use_detached_bar
import navic.composeapp.generated.resources.option_use_wavy_slider
import navic.composeapp.generated.resources.subtitle_enable_scrobbling
import navic.composeapp.generated.resources.subtitle_lyrics_beat_by_beat
import navic.composeapp.generated.resources.title_behaviour
import navic.composeapp.generated.resources.title_bottom_app_bar
import navic.composeapp.generated.resources.title_scrobbling
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.data.model.Settings
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.common.SettingSwitch
import paige.navic.ui.component.dialog.NavtabsDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.theme.mapleMono
import kotlin.math.roundToInt

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
					FormRow {
						Text(stringResource(Res.string.option_short_navigation_bar))
						SettingSwitch(
							checked = Settings.shared.useShortNavbar,
							onCheckedChange = { Settings.shared.useShortNavbar = it }
						)
					}
					FormRow {
						Text(stringResource(Res.string.option_use_detached_bar))
						SettingSwitch(
							checked = Settings.shared.detachedBar,
							onCheckedChange = { Settings.shared.detachedBar = it }
						)
					}
					FormRow {
						Text(stringResource(Res.string.option_swipe_to_skip))
						SettingSwitch(
							checked = Settings.shared.swipeToSkip,
							onCheckedChange = { Settings.shared.swipeToSkip = it }
						)
					}
					FormRow {
						Text(stringResource(Res.string.option_show_progress_in_bar))
						SettingSwitch(
							checked = Settings.shared.showProgressInBar,
							onCheckedChange = { Settings.shared.showProgressInBar = it }
						)
					}
					FormRow {
						Text(stringResource(Res.string.option_use_wavy_slider))
						SettingSwitch(
							checked = Settings.shared.useWavySlider,
							onCheckedChange = { Settings.shared.useWavySlider = it }
						)
					}
					FormRow(
						onClick = {
							showNavtabsDialog = true
						}
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
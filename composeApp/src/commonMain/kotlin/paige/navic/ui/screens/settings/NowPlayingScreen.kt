package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
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
import navic.composeapp.generated.resources.option_now_playing_toolbar_position
import navic.composeapp.generated.resources.option_player_background_style
import navic.composeapp.generated.resources.option_player_background_style_description
import navic.composeapp.generated.resources.option_player_slider_style
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.title_player
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.PlayerBackgroundStyle
import paige.navic.data.models.settings.enums.ToolbarPosition
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.dialogs.PlayerSliderStyleDialog
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.settings.SettingSelectionRow
import paige.navic.ui.components.settings.SettingSwitchRow
import paige.navic.utils.fadeFromTop

@Composable
fun SettingsNowPlayingScreen() {
	val ctx = LocalCtx.current

	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_player)) },
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
						items = PlayerBackgroundStyle.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.playerBackgroundStyle,
						onSelect = { Settings.shared.playerBackgroundStyle = it },
						description = stringResource(Res.string.option_player_background_style_description),
						title = { Text(stringResource(Res.string.option_player_background_style)) }
					)

					var showSliderStyleDialog by rememberSaveable { mutableStateOf(false) }
					FormRow(
						onClick = {
							showSliderStyleDialog = true
						}
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_player_slider_style))
							Text(
								stringResource(Settings.shared.playerSliderStyle.displayName),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					PlayerSliderStyleDialog(
						presented = showSliderStyleDialog,
						onDismissRequest = { showSliderStyleDialog = false }
					)

					SettingSelectionRow(
						items = ToolbarPosition.entries,
						label = { stringResource(it.displayName) },
						selection = Settings.shared.nowPlayingToolbarPosition,
						onSelect = { Settings.shared.nowPlayingToolbarPosition = it },
						title = { Text(stringResource(Res.string.option_now_playing_toolbar_position)) }
					)
				}
			}
		}
	}
}
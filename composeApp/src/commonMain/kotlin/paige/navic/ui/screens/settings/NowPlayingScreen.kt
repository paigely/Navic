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
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_now_playing_background_style
import navic.composeapp.generated.resources.option_now_playing_slider_style
import navic.composeapp.generated.resources.option_now_playing_toolbar_position
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.subtitle_now_playing_background_style
import navic.composeapp.generated.resources.title_now_playing
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NowPlayingBackgroundStyle
import paige.navic.data.models.settings.enums.ToolbarPosition
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSelectionRow
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.ui.screens.settings.dialogs.NowPlayingSliderStyleDialog

@Composable
fun SettingsNowPlayingScreen() {
	val ctx = LocalCtx.current

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_now_playing)) },
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
						items = NowPlayingBackgroundStyle.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = Settings.shared.nowPlayingBackgroundStyle,
						onSelect = { Settings.shared.nowPlayingBackgroundStyle = it },
						description = stringResource(Res.string.subtitle_now_playing_background_style),
						title = { Text(stringResource(Res.string.option_now_playing_background_style)) }
					)

					var showSliderStyleDialog by rememberSaveable { mutableStateOf(false) }
					FormRow(
						onClick = {
							showSliderStyleDialog = true
						}
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_now_playing_slider_style))
							Text(
								stringResource(Settings.shared.nowPlayingSliderStyle.displayName),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					NowPlayingSliderStyleDialog(
						presented = showSliderStyleDialog,
						onDismissRequest = { showSliderStyleDialog = false }
					)

					SettingSelectionRow(
						items = ToolbarPosition.entries.toImmutableList(),
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

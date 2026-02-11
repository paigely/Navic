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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.option_player_animate_background
import navic.composeapp.generated.resources.subtitle_lyrics_beat_by_beat
import navic.composeapp.generated.resources.title_now_playing
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.data.models.Settings
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.settings.SettingSwitchRow

@Composable
fun NowPlayingScreen() {
	val ctx = LocalCtx.current
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_now_playing)) },
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
						title = { Text(stringResource(Res.string.option_player_animate_background)) },
						value = Settings.shared.animatePlayerBackground,
						onSetValue = { Settings.shared.animatePlayerBackground = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_autoscroll)) },
						value = Settings.shared.lyricsAutoscroll,
						onSetValue = { Settings.shared.lyricsAutoscroll = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_beat_by_beat)) },
						subtitle = { Text(stringResource(Res.string.subtitle_lyrics_beat_by_beat)) },
						value = Settings.shared.lyricsBeatByBeat,
						onSetValue = { Settings.shared.lyricsBeatByBeat = it }
					)
				}
				Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
			}
		}
	}
}
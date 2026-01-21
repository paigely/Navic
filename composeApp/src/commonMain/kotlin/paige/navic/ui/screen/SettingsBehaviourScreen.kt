package paige.navic.ui.screen

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.subtitle_lyrics_beat_by_beat
import navic.composeapp.generated.resources.title_behaviour
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.common.SettingSwitch
import paige.navic.ui.component.layout.NestedTopBar

@Composable
fun SettingsBehaviourScreen() {
	var lyricsAutoscroll by rememberBooleanSetting("lyricsAutoscroll", true)
	var lyricsBeatByBeat by rememberBooleanSetting("lyricsBeatByBeat", true)
	val ctx = LocalCtx.current
	val hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_behaviour)) },
			hideBack = hideBack
		) }
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(12.dp)
					.padding(bottom = 117.9.dp)
			) {
				Form {
					FormRow {
						Text(stringResource(Res.string.option_lyrics_autoscroll))
						SettingSwitch(
							checked = lyricsAutoscroll,
							onCheckedChange = { lyricsAutoscroll = it }
						)
					}
					FormRow {
						Column {
							Text(stringResource(Res.string.option_lyrics_beat_by_beat))
							Text(
								stringResource(Res.string.subtitle_lyrics_beat_by_beat),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						SettingSwitch(
							checked = lyricsBeatByBeat,
							onCheckedChange = { lyricsBeatByBeat = it }
						)
					}
				}
			}
		}
	}
}
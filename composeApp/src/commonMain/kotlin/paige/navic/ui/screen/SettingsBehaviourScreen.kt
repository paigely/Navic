package paige.navic.ui.screen

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.option_min_duration_to_scrobble
import navic.composeapp.generated.resources.option_scrobble_percentage
import navic.composeapp.generated.resources.subtitle_lyrics_beat_by_beat
import navic.composeapp.generated.resources.title_behaviour
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.data.model.Settings
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.common.SettingSwitch
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.theme.mapleMono
import kotlin.math.roundToInt

@Composable
fun SettingsBehaviourScreen() {
	val ctx = LocalCtx.current
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_behaviour)) },
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
						Text(stringResource(Res.string.option_lyrics_autoscroll))
						SettingSwitch(
							checked = Settings.shared.lyricsAutoscroll,
							onCheckedChange = { Settings.shared.lyricsAutoscroll = it }
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
							checked = Settings.shared.lyricsBeatByBeat,
							onCheckedChange = { Settings.shared.lyricsBeatByBeat = it }
						)
					}
					FormRow {
						Column(Modifier.fillMaxWidth()) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(stringResource(Res.string.option_scrobble_percentage))
								Text(
									"${(Settings.shared.scrobblePercentage * 100).roundToInt()}%",
									fontFamily = mapleMono(),
									fontWeight = FontWeight(400),
									fontSize = 13.sp,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							Slider(
								value = Settings.shared.scrobblePercentage,
								onValueChange = {
									Settings.shared.scrobblePercentage = it
								},
								valueRange = 0f..1f,
							)
						}
					}
					FormRow {
						Column(Modifier.fillMaxWidth()) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(stringResource(Res.string.option_min_duration_to_scrobble))
								Text(
									"${Settings.shared.minDurationToScrobble.toInt()}s",
									fontFamily = mapleMono(),
									fontWeight = FontWeight(400),
									fontSize = 13.sp,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							Slider(
								value = Settings.shared.minDurationToScrobble,
								onValueChange = {
									Settings.shared.minDurationToScrobble = it
								},
								valueRange = 0f..400f,
							)
						}
					}
				}
				Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
			}
		}
	}
}
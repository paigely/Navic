package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_lyrics
import navic.composeapp.generated.resources.option_audio_offload
import navic.composeapp.generated.resources.option_enable_scrobbling
import navic.composeapp.generated.resources.option_gapless_playback
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.option_lyrics_blur
import navic.composeapp.generated.resources.option_lyrics_bright_inactive
import navic.composeapp.generated.resources.option_lyrics_keep_alive
import navic.composeapp.generated.resources.option_lyrics_priority
import navic.composeapp.generated.resources.option_min_duration_to_scrobble
import navic.composeapp.generated.resources.option_replay_gain
import navic.composeapp.generated.resources.option_scrobble_percentage
import navic.composeapp.generated.resources.subtitle_audio_offload
import navic.composeapp.generated.resources.subtitle_enable_scrobbling
import navic.composeapp.generated.resources.subtitle_gapless_playback
import navic.composeapp.generated.resources.subtitle_streaming_quality
import navic.composeapp.generated.resources.title_behaviour
import navic.composeapp.generated.resources.title_playback
import navic.composeapp.generated.resources.title_streaming_quality
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.ui.screens.settings.dialogs.LyricsPriorityDialog
import kotlin.math.roundToInt

@Composable
fun SettingsPlaybackScreen() {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	var showLyricsPriorityDialog by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_playback)) },
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
					FormRow(
						onClick = dropUnlessResumed { backStack.add(Screen.Settings.StreamingQuality) },
						horizontalArrangement = Arrangement.Start
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.title_streaming_quality))
							Text(
								text = stringResource(Res.string.subtitle_streaming_quality),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Icon(Icons.Outlined.ChevronForward, null)
					}
					if (!listOf("ipados", "ios").contains(ctx.name.lowercase())) {
						SettingSwitchRow(
							title = { Text(stringResource(Res.string.option_replay_gain)) },
							value = Settings.shared.replayGain,
							onSetValue = { Settings.shared.replayGain = it }
						)
						SettingSwitchRow(
							title = { Text(stringResource(Res.string.option_gapless_playback)) },
							subtitle = { Text(stringResource(Res.string.subtitle_gapless_playback)) },
							value = Settings.shared.gaplessPlayback,
							onSetValue = { Settings.shared.gaplessPlayback = it }
						)
						SettingSwitchRow(
							title = { Text(stringResource(Res.string.option_audio_offload)) },
							subtitle = { Text(stringResource(Res.string.subtitle_audio_offload)) },
							value = Settings.shared.audioOffload,
							onSetValue = { Settings.shared.audioOffload = it }
						)
					}
				}

				FormTitle(stringResource(Res.string.action_lyrics))
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_autoscroll)) },
						value = Settings.shared.lyricsAutoscroll,
						onSetValue = { Settings.shared.lyricsAutoscroll = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_beat_by_beat)) },
						value = Settings.shared.lyricsBeatByBeat,
						onSetValue = { Settings.shared.lyricsBeatByBeat = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_keep_alive)) },
						value = Settings.shared.lyricsKeepAlive,
						onSetValue = { Settings.shared.lyricsKeepAlive = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_blur)) },
						value = Settings.shared.lyricsBlur,
						onSetValue = { Settings.shared.lyricsBlur = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_bright_inactive)) },
						value = Settings.shared.lyricsBrightInactive,
						onSetValue = { Settings.shared.lyricsBrightInactive = it }
					)

					FormRow(
						onClick = { showLyricsPriorityDialog = true }
					) {
						Text(stringResource(Res.string.option_lyrics_priority))
					}
				}

				FormTitle(stringResource(Res.string.title_behaviour))
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_enable_scrobbling)) },
						subtitle = { Text(stringResource(Res.string.subtitle_enable_scrobbling)) },
						value = Settings.shared.enableScrobbling,
						onSetValue = { Settings.shared.enableScrobbling = it }
					)

					FormRow {
						Column(Modifier.fillMaxWidth()) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(stringResource(Res.string.option_scrobble_percentage))
								Text(
									"${(Settings.shared.scrobblePercentage * 100).roundToInt()}%",
									fontFamily = FontFamily.Monospace,
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
									fontFamily = FontFamily.Monospace,
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
			}
		}
		LyricsPriorityDialog(
			presented = showLyricsPriorityDialog,
			onDismissRequest = { showLyricsPriorityDialog = false }
		)
	}
}

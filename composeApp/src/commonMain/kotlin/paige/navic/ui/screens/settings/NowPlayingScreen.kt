package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_configure_lyric_providers
import navic.composeapp.generated.resources.action_lyrics
import navic.composeapp.generated.resources.option_cover_art_action
import navic.composeapp.generated.resources.option_lyrics_autoscroll
import navic.composeapp.generated.resources.option_lyrics_beat_by_beat
import navic.composeapp.generated.resources.option_lyrics_blur
import navic.composeapp.generated.resources.option_lyrics_bright_inactive
import navic.composeapp.generated.resources.option_lyrics_keep_alive
import navic.composeapp.generated.resources.option_now_playing_background_style
import navic.composeapp.generated.resources.option_now_playing_slider_style
import navic.composeapp.generated.resources.option_now_playing_song_info
import navic.composeapp.generated.resources.option_now_playing_toolbar_position
import navic.composeapp.generated.resources.option_swipe_to_skip
import navic.composeapp.generated.resources.subtitle_configure_lyric_providers
import navic.composeapp.generated.resources.subtitle_now_playing_background_style
import navic.composeapp.generated.resources.title_layout
import navic.composeapp.generated.resources.title_now_playing
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.CoverArtTapAction
import paige.navic.domain.models.settings.NowPlayingBackgroundStyle
import paige.navic.domain.models.settings.ToolbarPosition
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSelectionRow
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.LocalGlobalBottomBarHeight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import paige.navic.ui.screens.settings.dialogs.LyricsPrioritySheet
import paige.navic.ui.screens.settings.dialogs.NowPlayingSliderStyleDialog

@Composable
fun SettingsNowPlayingScreen() {
	val platformContext = LocalPlatformContext.current
	val preferenceManager = koinInject<PreferenceManager>()
	var lyricProvidersSheetOpen by rememberSaveable { mutableStateOf(false) }

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_now_playing)) },
				hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		},
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
					.padding(top = innerPadding.calculateTopPadding())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
			) {
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_swipe_to_skip)) },
						value = preferenceManager.swipeToSkip,
						onSetValue = { preferenceManager.swipeToSkip = it }
					)

					SettingSelectionRow(
						items = CoverArtTapAction.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = preferenceManager.nowPlayingCoverArtAction,
						onSelect = { preferenceManager.nowPlayingCoverArtAction = it },
						title = { Text(stringResource(Res.string.option_cover_art_action)) }
					)

					SettingSelectionRow(
						items = NowPlayingBackgroundStyle.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = preferenceManager.nowPlayingBackgroundStyle,
						onSelect = { preferenceManager.nowPlayingBackgroundStyle = it },
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
								stringResource(preferenceManager.nowPlayingSliderStyle.displayName),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					NowPlayingSliderStyleDialog(
						presented = showSliderStyleDialog,
						onDismissRequest = { showSliderStyleDialog = false }
					)
				}

				FormTitle(stringResource(Res.string.action_lyrics))
				Form {
					FormRow(
						onClick = { lyricProvidersSheetOpen = true }
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.action_configure_lyric_providers))
							Text(
								text = stringResource(Res.string.subtitle_configure_lyric_providers),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Icon(Icons.Outlined.ChevronForward, null)
					}

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_autoscroll)) },
						value = preferenceManager.lyricsAutoscroll,
						onSetValue = { preferenceManager.lyricsAutoscroll = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_beat_by_beat)) },
						value = preferenceManager.lyricsBeatByBeat,
						onSetValue = { preferenceManager.lyricsBeatByBeat = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_keep_alive)) },
						value = preferenceManager.lyricsKeepAlive,
						onSetValue = { preferenceManager.lyricsKeepAlive = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_blur)) },
						value = preferenceManager.lyricsBlur,
						onSetValue = { preferenceManager.lyricsBlur = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_lyrics_bright_inactive)) },
						value = preferenceManager.lyricsBrightInactive,
						onSetValue = { preferenceManager.lyricsBrightInactive = it }
					)
				}

				FormTitle(stringResource(Res.string.title_layout))
				Form {
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_now_playing_song_info)) },
						value = preferenceManager.nowPlayingSongInfo,
						onSetValue = { preferenceManager.nowPlayingSongInfo = it }
					)

					SettingSelectionRow(
						items = ToolbarPosition.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = preferenceManager.nowPlayingToolbarPosition,
						onSelect = { preferenceManager.nowPlayingToolbarPosition = it },
						title = { Text(stringResource(Res.string.option_now_playing_toolbar_position)) }
					)
				}
				Spacer(Modifier.height(LocalGlobalBottomBarHeight.current))
			}
		}
		LyricsPrioritySheet(
			presented = lyricProvidersSheetOpen,
			onDismissRequest = { lyricProvidersSheetOpen = false }
		)
	}
}

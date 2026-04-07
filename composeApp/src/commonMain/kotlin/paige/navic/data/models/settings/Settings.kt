// Adapted from https://github.com/zt64/tau/blob/main/core/src/main/kotlin/dev/zt64/tau/domain/manager/PreferencesManager.kt
// Copyright (c) 2025 zt64
// SPDX-License-Identifier: GPL-3.0

package paige.navic.data.models.settings

import com.russhwolf.settings.Settings as KmpSettings
import paige.navic.data.models.settings.enums.*

class Settings(
	settings: KmpSettings
) : BasePreferenceManager(settings) {
	var font by preference(FontOption.GoogleSans)
	var fontPath by preference("")
	var nowPlayingBackgroundStyle by preference(NowPlayingBackgroundStyle.Dynamic)
	var swipeToSkip by preference(true)
	var artGridRounding by preference(16f)
	var gridSize by preference(GridSize.TwoByTwo)
	var artGridItemSize by preference(150f)
	var marqueeSpeed by preference(MarqueeSpeed.Slow)
	var alphabeticalScroll by preference(false)
	var lyricsAutoscroll by preference(true)
	var lyricsBeatByBeat by preference(true)
	var lyricsKeepAlive by preference(true)
	var enableScrobbling by preference(true)
	var scrobblePercentage by preference(.5f)
	var minDurationToScrobble by preference(30f)
	var windowPlacement by preference(0)
	var windowPositionX by preference(100f)
	var windowPositionY by preference(100f)
	var windowSizeX by preference(800f)
	var windowSizeY by preference(600f)
	var nowPlayingToolbarPosition by preference(ToolbarPosition.Bottom)
	var replayGain by preference(false)
	var gaplessPlayback by preference(true)
	var audioOffload by preference(false)
	var nowPlayingSliderStyle by preference(NowPlayingSliderStyle.Squiggly)
	var customHeaders by preference("")
	var sleepTimerDuration by preference(0f)

	// navigation bar settings
	var bottomBarCollapseMode by preference(BottomBarCollapseMode.OnScroll)
	var bottomBarVisibilityMode by preference(BottomBarVisibilityMode.Default)
	var navigationBarStyle by preference(NavigationBarStyle.Normal)
	var miniPlayerStyle by preference(MiniPlayerStyle.Detached)
	var miniPlayerProgressStyle by preference(MiniPlayerProgressStyle.Seekable)

	/**
	 * If we have informed the user (on Android) about
	 * Google locking down sideloading.
	 */
	var showedSideloadingWarning by preference(false)

	// theme related settings
	var theme by preference(Theme.Dynamic)
	var themeMode by preference(ThemeMode.System)
	var accentColourH by preference(0f)
	var accentColourS by preference(0f)
	var accentColourV by preference(1f)

	// sync related settings
	var lastFullSyncTime by preference(0L)

	companion object {
		val shared = Settings(KmpSettings())
	}
}

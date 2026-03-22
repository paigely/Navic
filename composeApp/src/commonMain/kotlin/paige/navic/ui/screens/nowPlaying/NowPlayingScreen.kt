package paige.navic.ui.screens.nowPlaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NowPlayingBackgroundStyle
import paige.navic.data.models.settings.enums.ToolbarPosition
import paige.navic.ui.components.common.BlendBackground
import paige.navic.ui.screens.nowPlaying.components.controls.NowPlayingArtworkPager
import paige.navic.ui.screens.nowPlaying.components.rows.NowPlayingControlsRow
import paige.navic.utils.fadeFromTop
import paige.navic.utils.rememberTrackPainter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NowPlayingScreen() {
	val player = LocalMediaPlayer.current
	val backStack = LocalNavStack.current

	val currentScreen = backStack.lastOrNull()
	val isPlayerCurrent = currentScreen is Screen.NowPlaying

	val playerState by player.uiState.collectAsState()
	val track = playerState.currentTrack

	val sharedPainter = rememberTrackPainter(track?.id, track?.coverArtId)

	Box(Modifier.fillMaxSize()) {
		when (Settings.shared.nowPlayingBackgroundStyle) {
			NowPlayingBackgroundStyle.Static -> Unit
			NowPlayingBackgroundStyle.Dynamic -> {
				BlendBackground(
					painter = sharedPainter,
					isPaused = playerState.isPaused
				)
			}
		}
		if (!isPlayerCurrent) return@Box
		BoxWithConstraints(
			modifier = Modifier
				.padding(horizontal = 8.dp)
				.navigationBarsPadding()
				.statusBarsPadding()
				.fillMaxSize()
				.fadeFromTop()
		) {
			val isLandscape = maxWidth > maxHeight
			val padding = if (Settings.shared.nowPlayingToolbarPosition == ToolbarPosition.Top)
				PaddingValues(top = if (isLandscape) 50.dp else 90.dp)
			else PaddingValues(
				top = if (isLandscape) 0.dp else 50.dp,
				bottom = 50.dp
			)
			if (isLandscape) {
				Row(
					modifier = Modifier.fillMaxSize().padding(padding),
					horizontalArrangement = Arrangement.SpaceEvenly,
					verticalAlignment = Alignment.CenterVertically
				) {
					NowPlayingArtworkPager(
						modifier = Modifier.weight(1f).fillMaxHeight(),
						isLandscape = true
					)
					NowPlayingControlsRow(
						modifier = Modifier.weight(1f).fillMaxHeight(),
						isLandscape = true
					)
				}
			} else {
				Column(
					modifier = Modifier.fillMaxSize().padding(padding),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					NowPlayingArtworkPager(
						modifier = Modifier.weight(1f).fillMaxWidth(),
						isLandscape = false
					)
					NowPlayingControlsRow(
						modifier = Modifier.weight(1f),
						isLandscape = false
					)
				}
			}
		}
	}
}

package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import paige.navic.LocalMediaPlayer
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NowPlayingSliderStyle

@Composable
fun NowPlayingProgressBar() {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsState()
	val waveHeight by animateDpAsState(
		if (!playerState.isPaused
			&& Settings.shared.nowPlayingSliderStyle == NowPlayingSliderStyle.Squiggly)
			6.dp
		else 0.dp
	)

	@OptIn(ExperimentalMaterial3Api::class)
	WavySlider(
		value = playerState.progress,
		onValueChange = { player.seek(it) },
		waveHeight = waveHeight,
		modifier = Modifier.padding(start = 16.dp, end = 13.5.dp),
		thumb = {
			SliderDefaults.Thumb(
				enabled = playerState.currentTrack != null,
				thumbSize = DpSize(4.dp, 32.dp),
				interactionSource = remember { MutableInteractionSource() }
			)
		},
		enabled = playerState.currentTrack != null,
		animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
			waveAppearanceAnimationSpec = snap()
		)
	)
}
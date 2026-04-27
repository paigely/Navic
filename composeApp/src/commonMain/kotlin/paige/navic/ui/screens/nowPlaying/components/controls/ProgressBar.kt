package paige.navic.ui.screens.nowPlaying.components.controls

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.WaveDirection
import ir.mahozad.multiplatform.wavyslider.WaveVelocity
import ir.mahozad.multiplatform.wavyslider.material3.Track
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WaveVelocity
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NowPlayingSliderStyle
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.SlimSlider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingProgressBar() {
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val enabled = playerState.currentSong != null
	val waveHeight by animateDpAsState(
		if (!playerState.isPaused)
			6.dp
		else 0.dp
	)

	when (Settings.shared.nowPlayingSliderStyle) {
		NowPlayingSliderStyle.Flat -> {
			Slider(
				value = playerState.progress,
				onValueChange = { player.seek(it) },
				modifier = Modifier.padding(horizontal = 16.dp),
				enabled = enabled
			)
		}
		NowPlayingSliderStyle.Squiggly, NowPlayingSliderStyle.Yoyo -> {
			val isYoyo = Settings.shared.nowPlayingSliderStyle == NowPlayingSliderStyle.Yoyo
			WavySlider(
				value = playerState.progress,
				onValueChange = { player.seek(it) },
				modifier = Modifier.padding(
					horizontal = if (isYoyo) 7.dp else 14.dp
				),
				waveHeight = waveHeight,
				thumb = {
					SliderDefaults.Thumb(
						enabled = playerState.currentSong != null,
						thumbSize = if (isYoyo)
							DpSize(20.dp, 20.dp)
						else DpSize(4.dp, 32.dp),
						interactionSource = remember { MutableInteractionSource() }
					)
				},
				track = { sliderState ->
					SliderDefaults.Track(
						sliderState = sliderState,
						thumbTrackGapSize = 0.dp,
						waveLength = if (isYoyo) 32.dp else 26.dp,
						waveHeight = waveHeight,
						animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
							waveAppearanceAnimationSpec = snap()
						),
						waveVelocity = if (isYoyo)
							WaveVelocity(14.dp, WaveDirection.TAIL)
						else SliderDefaults.WaveVelocity
					)
				},
				enabled = enabled
			)
		}
		NowPlayingSliderStyle.Slim -> {
			SlimSlider(
				value = playerState.progress,
				onValueChange = { player.seek(it) },
				modifier = Modifier.padding(horizontal = 16.dp),
				enabled = enabled
			)
		}
	}
}

package paige.navic.ui.screens.settings.dialogs

import androidx.compose.animation.core.snap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.material3.Track
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WaveLength
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_now_playing_slider_style
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NowPlayingSliderStyle
import paige.navic.ui.components.common.SlimSlider

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NowPlayingSliderStyleDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val ctx = LocalCtx.current
	var sliderValue by rememberSaveable { mutableFloatStateOf(0.6767f) }

	AlertDialog(
		title = {
			Text(stringResource(Res.string.option_now_playing_slider_style))
		},
		text = {
			LazyVerticalGrid(
				modifier = Modifier
					.fillMaxWidth()
					.heightIn(max = 300.dp),
				columns = GridCells.Fixed(2),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				NowPlayingSliderStyle.entries.forEach { style ->
					item(key = style.ordinal) {
						Option(
							onClick = {
								Settings.shared.nowPlayingSliderStyle = style
							},
							selected = Settings.shared.nowPlayingSliderStyle == style,
							label = stringResource(style.displayName)
						) {
							when (style) {
								NowPlayingSliderStyle.Flat -> {
									Slider(
										value = sliderValue,
										onValueChange = { sliderValue = it },
										modifier = Modifier.requiredWidth(200.dp).scale(.5f)
									)
								}

								NowPlayingSliderStyle.Squiggly -> {
									WavySlider(
										value = sliderValue,
										onValueChange = { sliderValue = it },
										modifier = Modifier.requiredWidth(200.dp).scale(.5f),
										track = { sliderState ->
											SliderDefaults.Track(
												sliderState = sliderState,
												thumbTrackGapSize = 0.dp,
												waveLength = SliderDefaults.WaveLength,
												animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
													waveAppearanceAnimationSpec = snap()
												),
											)
										}
									)
								}

								NowPlayingSliderStyle.Yoyo -> {
									WavySlider(
										value = sliderValue,
										onValueChange = { sliderValue = it },
										modifier = Modifier.requiredWidth(200.dp).scale(.5f),
										track = { sliderState ->
											SliderDefaults.Track(
												sliderState = sliderState,
												thumbTrackGapSize = 0.dp,
												waveLength = 24.dp,
												animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
													waveAppearanceAnimationSpec = snap()
												),
											)
										},
										thumb = { sliderState ->
											SliderDefaults.Thumb(
												interactionSource = remember { MutableInteractionSource() },
												sliderState = sliderState,
												thumbSize = DpSize(16.dp, 16.dp),
												modifier = Modifier.clip(CircleShape)
											)
										}
									)
								}

								NowPlayingSliderStyle.Slim -> {
									SlimSlider(
										value = sliderValue,
										onValueChange = { sliderValue = it },
										modifier = Modifier.requiredWidth(200.dp).scale(.5f)
									)
								}
							}
						}
					}
				}
			}
		},
		onDismissRequest = onDismissRequest,
		confirmButton = {
			Button(onClick = {
				ctx.clickSound()
				onDismissRequest()
			}) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	)
}

@Composable
private fun Option(
	onClick: () -> Unit,
	selected: Boolean,
	label: String,
	content: @Composable () -> Unit
) {
	val ctx = LocalCtx.current
	Card(
		border = BorderStroke(
			width = 1.dp,
			color = if (selected)
				MaterialTheme.colorScheme.primary
			else MaterialTheme.colorScheme.outlineVariant
		),
		shape = MaterialTheme.shapes.large,
		onClick = {
			onClick()
			ctx.clickSound()
		}
	) {
		Column {
			Box(
				modifier = Modifier
					.padding(8.dp)
					.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				content()
			}
			Box(
				modifier = Modifier
					.padding(12.dp)
					.fillMaxWidth(),
				contentAlignment = Alignment.Center
			) {
				Text(label)
			}
		}
	}
}

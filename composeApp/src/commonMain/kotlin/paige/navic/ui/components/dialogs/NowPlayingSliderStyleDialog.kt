package paige.navic.ui.components.dialogs

import androidx.compose.animation.core.snap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_now_playing_slider_style
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.NowPlayingSliderStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingSliderStyleDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val ctx = LocalCtx.current

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
										rememberSliderState(0.6767f),
										modifier = Modifier.requiredWidth(200.dp).scale(.5f)
									)
								}
								NowPlayingSliderStyle.Squiggly -> {
									WavySlider(
										rememberSliderState(0.6767f),
										modifier = Modifier.requiredWidth(200.dp).scale(.5f),
										animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
											waveAppearanceAnimationSpec = snap()
										)
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
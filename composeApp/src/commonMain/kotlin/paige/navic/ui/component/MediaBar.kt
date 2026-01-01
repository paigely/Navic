package paige.navic.ui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.pause
import navic.composeapp.generated.resources.skip_next
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBar() {
	val ctx = LocalCtx.current
	var progress by remember { mutableFloatStateOf(0.314f) }
	val interactionSource = remember { MutableInteractionSource() }
	Surface(
		shape = ContinuousRoundedRectangle(24.dp, 24.dp, 0.dp, 0.dp),
		color = NavigationBarDefaults.containerColor,
		modifier = Modifier
			.dropShadow(
				shape = ContinuousRoundedRectangle(24.dp),
				shadow = Shadow(
					radius = 10.dp,
					color = MaterialTheme.colorScheme.scrim.copy(alpha = .5f)
				)
			)
	) {
		Column {
			Row(
				modifier = Modifier
					.padding(
						top = 15.dp,
						start = 15.dp,
						end = 15.dp,
						bottom = 0.dp
					)
					.fillMaxWidth()
					.height(IntrinsicSize.Min),
				horizontalArrangement = Arrangement.spacedBy(10.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Surface(
					modifier = Modifier.size(55.dp),
					shape = ContinuousRoundedRectangle(12.dp),
					color = MaterialTheme.colorScheme.surfaceVariant
				) {
				}
				Column(
					modifier = Modifier
						.fillMaxHeight()
						.weight(1f),
					verticalArrangement = Arrangement.Center
				) {
					Text("Title", fontWeight = FontWeight(600))
					Text("Artist", style = MaterialTheme.typography.titleSmall)
				}
				IconButton(
					onClick = {
						ctx.clickSound()
					}
				) {
					Icon(
						vectorResource(Res.drawable.pause),
						contentDescription = null
					)
				}
				IconButton(
					onClick = {
						ctx.clickSound()
					}
				) {
					Icon(
						vectorResource(Res.drawable.skip_next),
						contentDescription = null
					)
				}
			}
			WavySlider(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						top = 0.dp,
						start = 15.dp,
						end = 15.dp,
						bottom = 0.dp,
					),
				value = progress,
				onValueChange = { progress = it },
				thumb = {
					SliderDefaults.Thumb(
						interactionSource = interactionSource,
						colors = SliderDefaults.colors(),
						enabled = true,
						thumbSize = DpSize(6.dp, 24.dp),
						modifier = Modifier.clip(ContinuousCapsule)
					)
				}
			)
		}
	}
}

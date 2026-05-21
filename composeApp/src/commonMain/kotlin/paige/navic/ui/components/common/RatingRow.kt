package paige.navic.ui.components.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_rate_stars
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Star

@Composable
fun RatingRow(
	rating: Int,
	setRating: (Int) -> Unit
) {
	val ctx = LocalCtx.current
	var visible by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(Unit) {
		visible = true
	}

	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(
			4.dp,
			Alignment.CenterHorizontally
		)
	) {
		(1..5).forEach { idx ->
			val progress by animateFloatAsState(
				if (visible) 1f else 0f,
				animationSpec = tween(
					delayMillis = idx * 25
				)
			)

			IconButton(
				onClick = {
					ctx.clickSound()
					if (rating == idx) {
						setRating(0)
					} else {
						setRating(idx)
					}
				},
				modifier = Modifier.graphicsLayer {
					scaleX = progress
					scaleY = progress
					alpha = progress
				}
			) {
				Icon(
					imageVector = if (idx <= rating)
						Icons.Filled.Star
					else Icons.Outlined.Star,
					contentDescription = pluralStringResource(
						Res.plurals.count_rate_stars, idx, idx
					),
					tint = MaterialTheme.colorScheme.primary
				)
			}
		}
	}
}

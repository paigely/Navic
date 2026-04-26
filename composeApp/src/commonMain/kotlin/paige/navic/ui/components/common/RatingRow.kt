package paige.navic.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Star
import paige.navic.ui.theme.yellow

@Composable
fun RatingRow(
	rating: Int,
	setRating: (Int) -> Unit
) {
	val ctx = LocalCtx.current
	Row {
		repeat(5) { idx ->
			val inBounds = (idx + 1) <= rating
			Icon(
				imageVector = if (inBounds)
					Icons.Filled.Star
				else Icons.Outlined.Star,
				contentDescription = null,
				modifier = Modifier
					.size(24.dp)
					.clip(CircleShape)
					.clickable {
						ctx.clickSound()
						if (rating == idx + 1) {
							setRating(0)
						} else {
							setRating(idx + 1)
						}
					},
				tint = if (inBounds)
					MaterialTheme.colorScheme.yellow
				else MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

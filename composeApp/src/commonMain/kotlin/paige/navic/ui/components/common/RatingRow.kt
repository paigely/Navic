package paige.navic.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.count_rate_stars
import org.jetbrains.compose.resources.pluralStringResource
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Star

@Composable
fun RatingRow(
	rating: Int,
	setRating: (Int) -> Unit
) {

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.selectableGroup(),
		horizontalArrangement = Arrangement.spacedBy(
			4.dp,
			Alignment.CenterHorizontally
		)
	) {
		(1..5).forEach { idx ->
			IconButton(
				modifier = Modifier.semantics {
					selected = rating == idx
				},
				onClick = {
					if (rating == idx) {
						setRating(0)
					} else {
						setRating(idx)
					}
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

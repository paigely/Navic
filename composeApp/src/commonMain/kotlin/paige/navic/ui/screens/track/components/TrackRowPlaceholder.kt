package paige.navic.ui.screens.track.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.utils.shimmerLoading

fun LazyListScope.tracksScreenTrackRowPlaceholder(
	rowCount: Int? = null
) {
	val rowCount = rowCount ?: 10
	item {
		Row(
			modifier = Modifier.padding(horizontal = 31.dp, vertical = 31.dp),
			horizontalArrangement = Arrangement.spacedBy(
				10.dp,
				alignment = Alignment.CenterHorizontally
			)
		) {
			Box(Modifier.weight(1f).height(40.dp).clip(MaterialTheme.shapes.medium).shimmerLoading())
			Box(Modifier.weight(1f).height(40.dp).clip(MaterialTheme.shapes.medium).shimmerLoading())
			Box(Modifier.size(40.dp).clip(MaterialTheme.shapes.medium).shimmerLoading())
		}
	}
	items(rowCount) { idx ->
		Box(
			modifier = Modifier
				.padding(
					bottom = 3.dp,
					start = 16.dp,
					end = 16.dp
				)
				.clip(
					if (rowCount == 1) {
						ContinuousRoundedRectangle(18.dp)
					} else {
						when (idx) {
							0 -> ContinuousRoundedRectangle(
								topStart = 18.dp,
								topEnd = 18.dp
							)

							rowCount - 1 -> ContinuousRoundedRectangle(
								bottomStart = 18.dp,
								bottomEnd = 18.dp
							)

							else -> ContinuousRoundedRectangle(3.dp)
						}
					}
				)
				.shimmerLoading()
				.fillMaxWidth()
				.height(72.dp)
		)
	}
}

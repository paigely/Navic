package paige.navic.utils

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun segmentedShapes(
	index: Int,
	count: Int,
	defaultShapes: ListItemShapes = ListItemDefaults.shapes(),
	dismissDirection: SwipeToDismissBoxValue? = null
): ListItemShapes {
	val overrideShape = ContinuousRoundedRectangle(18.dp)
	return remember(index, count, defaultShapes, overrideShape, dismissDirection) {
		when {
			count == 1 || (dismissDirection != SwipeToDismissBoxValue.Settled && dismissDirection != null) -> {
				defaultShapes.copy(
					shape = overrideShape
				)
			}

			index == 0 -> {
				val defaultBaseShape = defaultShapes.shape
				if (defaultBaseShape is CornerBasedShape) {
					defaultShapes.copy(
						shape =
							defaultBaseShape.copy(
								topStart = overrideShape.topStart,
								topEnd = overrideShape.topEnd,
							)
					)
				} else {
					defaultShapes
				}
			}

			index == count - 1 -> {
				val defaultBaseShape = defaultShapes.shape
				if (defaultBaseShape is CornerBasedShape) {
					defaultShapes.copy(
						shape =
							defaultBaseShape.copy(
								bottomStart = overrideShape.bottomStart,
								bottomEnd = overrideShape.bottomEnd,
							)
					)
				} else {
					defaultShapes
				}
			}

			else -> defaultShapes
		}
	}
}

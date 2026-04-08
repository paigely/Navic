package paige.navic.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource

class BottomBarScrollManager(val thresholdPx: Float) {
	var isTriggered by mutableStateOf(false)
	private var accumulator = 0f

	val connection = object : NestedScrollConnection {
		override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
			val delta = available.y
			accumulator += delta

			if (accumulator < -thresholdPx && !isTriggered) {
				isTriggered = true
				accumulator = 0f
			} else if (accumulator > thresholdPx && isTriggered) {
				isTriggered = false
				accumulator = 0f
			}

			if ((delta > 0 && accumulator < 0) || (delta < 0 && accumulator > 0)) {
				accumulator = 0f
			}
			return Offset.Zero
		}
	}
}

val LocalBottomBarScrollManager = staticCompositionLocalOf<BottomBarScrollManager> {
	error("No BottomBarScrollManager provided")
}

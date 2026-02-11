package paige.navic.ui.components.common

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import paige.navic.LocalContentPadding
import paige.navic.data.models.Settings
import kotlin.math.abs

@Composable
fun AlphabeticalScroller(
	state: LazyGridState,
	headers: List<Pair<String, Int>>
) {
	if (!Settings.shared.alphabeticalScroll) return
	val haptic = LocalHapticFeedback.current
	val scope = rememberCoroutineScope()
	val offsets = remember { mutableStateMapOf<Int, Float>() }

	var lastSelectedIndex by remember { mutableStateOf(-1) }

	fun updateSelection(yCoordinate: Float) {
		val closestEntry = offsets.entries
			.minByOrNull { abs(it.value - yCoordinate) } ?: return
		val newIndex = closestEntry.key
		if (newIndex != lastSelectedIndex) {
			lastSelectedIndex = newIndex
			haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
			scope.launch {
				state.scrollToItem(headers[newIndex].second)
			}
		}
	}

	Column(
		verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
		modifier = Modifier
			.fillMaxHeight()
			.padding(bottom = LocalContentPadding.current.calculateBottomPadding())
			.pointerInput(headers) {
				detectDragGestures(
					onDrag = { change, _ -> updateSelection(change.position.y) },
					onDragStart = { offset -> updateSelection(offset.y) },
					onDragEnd = { lastSelectedIndex = -1 }
				)
			}
	) {
		headers.forEachIndexed { i, (letter, _) ->
			Text(
				text = letter,
				modifier = Modifier.width(20.dp).onGloballyPositioned {
					offsets[i] = it.positionInParent().y + (it.size.height / 2f)
				},
				textAlign = TextAlign.Center,
				color = MaterialTheme.colorScheme.secondary
			)
		}
	}
}

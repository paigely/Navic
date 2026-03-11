/*
 * Copyright 2024 Allan Veloso Lopes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Temporarily implementation until this issue is made available on the official library
// https://issuetracker.google.com/issues/356619939
// Based on https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/foundation/foundation/integration-tests/foundation-demos/src/main/java/androidx/compose/foundation/demos/LazyColumnDragAndDropDemo.kt
// https://issuetracker.google.com/issues/181282427

package paige.navic.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * Creates a [DraggableListState] that is remembered across compositions.
 *
 * @param lazyListState the [LazyListState] whose items are being dragged.
 * @param onMove the callback that is triggered when an item is moved.
 */
@Composable
fun rememberDraggableListState(
	lazyListState: LazyListState = rememberLazyListState(),
	onMove: (Int, Int) -> Unit
): DraggableListState {
	val scope = rememberCoroutineScope()
	val state = remember(lazyListState) {
		DraggableListState(
			listState = lazyListState,
			onMove = onMove,
			scope = scope
		)
	}
	LaunchedEffect(state) {
		while (true) {
			val diff = state.scrollChannel.receive()
			lazyListState.scrollBy(diff)
		}
	}
	return state
}

/**
 * A state object that can be hoisted to control and observe dragging within a list.
 *
 * In most cases, this will be created via [rememberLazyListState].
 *
 * @param listState the state of the list where the item is being dragged.
 * @param scope a coroutine scope for performing animations.
 * @param onMove the callback that is triggered when an item is moved. The callback is
 * invoked whenever the dragged item middle point passes another item top or bottom offset,
 * it does not depend on the drag to be completed.
 */
class DraggableListState internal constructor(
	val listState: LazyListState,
	private val scope: CoroutineScope,
	private val onMove: (Int, Int) -> Unit
) {
	var draggingItemIndex by mutableStateOf<Int?>(null)
		private set

	internal val scrollChannel = Channel<Float>()

	private var draggingItemDraggedDelta by mutableFloatStateOf(0f)

	private var draggingItemInitialOffset by mutableIntStateOf(0)

	val draggingItemOffset: Float
		get() = draggingItemLayoutInfo?.let { item ->
			draggingItemInitialOffset + draggingItemDraggedDelta - item.offset
		} ?: 0f

	private val draggingItemLayoutInfo: LazyListItemInfo?
		get() = listState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggingItemIndex }

	var previousIndexOfDraggedItem by mutableStateOf<Int?>(null)
		private set

	var previousItemOffset = Animatable(0f)
		private set

	internal fun onDragStart(index: Int) {
		listState.layoutInfo.visibleItemsInfo
			.find { it.index == index }
			?.also {
				draggingItemIndex = it.index
				draggingItemInitialOffset = it.offset
			}
	}

	internal fun onDragStart(key: Any) {
		listState.layoutInfo.visibleItemsInfo
			.find { it.key == key }
			?.also {
				draggingItemIndex = it.index
				draggingItemInitialOffset = it.offset
			}
	}

	internal fun onDragInterrupted() {
		if (draggingItemIndex != null) {
			previousIndexOfDraggedItem = draggingItemIndex
			val startOffset = draggingItemOffset
			scope.launch {
				previousItemOffset.snapTo(startOffset)
				previousItemOffset.animateTo(
					0f,
					spring(stiffness = Spring.StiffnessMediumLow, visibilityThreshold = 1f)
				)
				previousIndexOfDraggedItem = null
			}
		}
		draggingItemDraggedDelta = 0f
		draggingItemIndex = null
		draggingItemInitialOffset = 0
	}

	internal fun onDrag(offset: Offset) {
		draggingItemDraggedDelta += offset.y

		val draggingItem = draggingItemLayoutInfo ?: return
		val startOffset = draggingItem.offset + draggingItemOffset
		val endOffset = startOffset + draggingItem.size
		val middleOffset = startOffset + (endOffset - startOffset) / 2f

		val targetItem = listState.layoutInfo.visibleItemsInfo.find { item ->
			middleOffset.toInt() in item.offset..item.offsetEnd &&
				draggingItem.index != item.index
		}

		if (targetItem != null) {
			if (
				draggingItem.index == listState.firstVisibleItemIndex ||
				targetItem.index == listState.firstVisibleItemIndex
			) {
				listState.requestScrollToItem(
					listState.firstVisibleItemIndex,
					listState.firstVisibleItemScrollOffset
				)
			}
			onMove(draggingItem.index, targetItem.index)
			draggingItemIndex = targetItem.index
		} else {
			val overscroll = when {
				draggingItemDraggedDelta > 0 ->
					(endOffset - listState.layoutInfo.viewportEndOffset).coerceAtLeast(0f)

				draggingItemDraggedDelta < 0 ->
					(startOffset - listState.layoutInfo.viewportStartOffset).coerceAtMost(0f)

				else -> 0f
			}
			if (overscroll != 0f) {
				scrollChannel.trySend(overscroll)
			}
		}
	}

	private val LazyListItemInfo.offsetEnd: Int
		get() = this.offset + this.size
}

/**
 * Defines the area that will act as the handle for dragging operations.
 *
 * @param state The draggable state of the list where the handler is being used.
 * @param index The index of the item for which the handler is being used.
 * @param onlyAfterLongPress Indicates whether dragging should begin only after a long press.
 */
fun Modifier.dragHandle(
	state: DraggableListState,
	index: Int,
	onlyAfterLongPress: Boolean = false
): Modifier {
	return pointerInput(state) {
		if (onlyAfterLongPress) {
			detectDragGesturesAfterLongPress(
				onDrag = { change, offset ->
					change.consume()
					state.onDrag(offset = offset)
				},
				onDragStart = { state.onDragStart(index = index) },
				onDragEnd = { state.onDragInterrupted() },
				onDragCancel = { state.onDragInterrupted() }
			)
		} else {
			detectDragGestures(
				onDrag = { change, offset ->
					change.consume()
					state.onDrag(offset = offset)
				},
				onDragStart = { state.onDragStart(index = index) },
				onDragEnd = { state.onDragInterrupted() },
				onDragCancel = { state.onDragInterrupted() }
			)
		}
	}
}

/**
 * Defines the area that will act as the handle for dragging operations.
 *
 * If you use this version of the [dragHandle], you must implement the key parameter on the
 * [draggableItems] or [draggableItemsIndexed].
 *
 * @param state The draggable state of the list where the handler is being used.
 * @param key The unique identifier for the item within the list for which the handler is being used.
 * @param onlyAfterLongPress Indicates whether dragging should begin only after a long press.
 */
fun Modifier.dragHandle(
	state: DraggableListState,
	key: Any,
	onlyAfterLongPress: Boolean = false
): Modifier {
	return pointerInput(state) {
		if (onlyAfterLongPress) {
			detectDragGesturesAfterLongPress(
				onDrag = { change, offset ->
					change.consume()
					state.onDrag(offset = offset)
				},
				onDragStart = { state.onDragStart(key = key) },
				onDragEnd = { state.onDragInterrupted() },
				onDragCancel = { state.onDragInterrupted() }
			)
		} else {
			detectDragGestures(
				onDrag = { change, offset ->
					change.consume()
					state.onDrag(offset = offset)
				},
				onDragStart = { state.onDragStart(key = key) },
				onDragEnd = { state.onDragInterrupted() },
				onDragCancel = { state.onDragInterrupted() }
			)
		}
	}
}

/**
 * Adds a list of draggable items.
 *
 * To the items to be draggable, it's also necessary to add the modifier [dragHandle] to
 * the preferable UI element of the item which will be the target of the drag operation.
 *
 * @param items the data list
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one. This can be overridden by calling 'requestScrollToItem'
 * on the 'LazyListState'.
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazyListScope.draggableItemsIndexed(
	state: DraggableListState,
	items: List<T>,
	noinline key: ((index: Int, item: T) -> Any)? = null,
	crossinline contentType: (index: Int, item: T) -> Any? = { _, _ -> null },
	crossinline itemContent: @Composable LazyItemScope.(index: Int, item: T, isDragging: Boolean) -> Unit
) = itemsIndexed(
	items = items,
	key = key,
	contentType = contentType
) { index, item ->

	val isDragging = index == state.draggingItemIndex
	val draggingModifier = if (isDragging) {
		Modifier
			.zIndex(1f)
			.graphicsLayer { translationY = state.draggingItemOffset }
	} else if (index == state.previousIndexOfDraggedItem) {
		Modifier
			.zIndex(1f)
			.graphicsLayer { translationY = state.previousItemOffset.value }
	} else {
		Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
	}
	Box(modifier = draggingModifier) {
		itemContent(index, item, isDragging)
	}
}

/**
 * Adds a list of draggable items.
 *
 * To the items to be draggable, it's also necessary to add the modifier [dragHandle] to
 * the preferable UI element of the item which will be the target of the drag operation.
 *
 * @param items the data list
 * @param key a factory of stable and unique keys representing the item. Using the same key
 * for multiple items in the list is not allowed. Type of the key should be saveable
 * via Bundle on Android. If null is passed the position in the list will represent the key.
 * When you specify the key the scroll position will be maintained based on the key, which
 * means if you add/remove items before the current visible item the item with the given key
 * will be kept as the first visible one. This can be overridden by calling 'requestScrollToItem'
 * on the 'LazyListState'.
 * @param contentType a factory of the content types for the item. The item compositions of
 * the same type could be reused more efficiently. Note that null is a valid type and items of such
 * type will be considered compatible.
 * @param itemContent the content displayed by a single item
 */
inline fun <T> LazyListScope.draggableItems(
	state: DraggableListState,
	items: List<T>,
	noinline key: ((item: T) -> Any)? = null,
	crossinline contentType: (item: T) -> Any? = { _ -> null },
	crossinline itemContent: @Composable LazyItemScope.(item: T, isDragging: Boolean) -> Unit
) = draggableItemsIndexed(
	state = state,
	items = items,
	key = key?.let { block -> { _, item -> block(item) } },
	contentType = { _, item -> contentType(item) },
	itemContent = { _, item, isDragging -> itemContent(item, isDragging) }
)

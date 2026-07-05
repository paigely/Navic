package paige.navic.ui.screens.lyrics.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_lyrics_provider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.lyrics.LyricsResult
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.util.core.calculateWordProgress
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun LyricsScreenContent(
	data: LyricsResult?,
	onRefresh: () -> Unit,
	isSelecting: Boolean,
	selectedIndices: ImmutableList<Int>,
	onAddSelectedIndex: (Int) -> Unit,
	onRemoveSelectedIndex: (Int) -> Unit,
	onRestartAtIndex: (Int) -> Unit,
	duration: Duration,
	currentDuration: Duration,
	contentPadding: PaddingValues
) {
	val density = LocalDensity.current
	val player = koinInject<MediaPlayerViewModel>()
	val preferenceManager = koinInject<PreferenceManager>()

	val listState = rememberLazyListState()

	val lyrics = data?.lines
	val isSynced = data?.isSynced == true
	val provider = data?.provider
	val maxSelectionChars = 150
	fun totalSelectedChars(): Int =
		selectedIndices.sumOf { lyrics?.getOrNull(it)?.text?.length ?: 0 }

	if (lyrics.isNullOrEmpty()) return LyricsScreenPlaceholder(onRefresh = onRefresh)

	val activeIndex = if (isSynced) {
		lyrics.indexOfLast { line ->
			line.time != null && currentDuration >= line.time
		}
	} else -1

	val lyricsAutoscroll = preferenceManager.lyricsAutoscroll
		&& !isSelecting
		&& isSynced

	LaunchedEffect(activeIndex, isSelecting) {
		if (!lyricsAutoscroll) return@LaunchedEffect

		val layoutInfo = listState.layoutInfo
		val activeItem = layoutInfo.visibleItemsInfo
			.firstOrNull { it.index == activeIndex }

		if (activeItem != null) {
			val itemCenter = activeItem.offset + activeItem.size / 2
			val viewportCenter =
				(layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
			val distance = itemCenter - viewportCenter
			val thresholdPx = with(density) { 24.dp.toPx() }

			if (abs(distance) > thresholdPx) {
				listState.animateScrollBy(
					value = distance.toFloat(),
					animationSpec = spring(
						stiffness = Spring.StiffnessLow,
						dampingRatio = Spring.DampingRatioNoBouncy
					)
				)
			}
		} else if (activeIndex >= 0) {
			launch {
				delay(500.milliseconds)
				val viewportCenter =
					(layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
				val scrollOffset = -(viewportCenter / 2)

				listState.animateScrollToItem(
					index = activeIndex,
					scrollOffset = scrollOffset
				)
			}
		}
	}

	LazyColumn(
		Modifier.fillMaxSize(),
		state = listState,
		contentPadding = contentPadding + PaddingValues(top = 16.dp)
	) {
		itemsIndexed(lyrics) { index, line ->
			val isActive = if (isSynced) index == activeIndex else true
			val isSelected = selectedIndices.contains(index)
			val isEmphasised = (isActive && !isSelecting) || isSelected
			val distance = abs(index - activeIndex)

			val lineTime = line.time ?: 0.milliseconds
			val preEmphasis = 200.milliseconds
			val nextTime = lyrics.getOrNull(index + 1)?.time ?: duration
			val lineDuration =
				(nextTime - lineTime).coerceAtLeast(1.milliseconds)
			val effectiveStart = lineTime - preEmphasis
			val effectiveDuration = lineDuration + preEmphasis

			val lineProgress = when {
				currentDuration < effectiveStart -> 0f
				currentDuration >= effectiveStart + effectiveDuration -> 1f
				else -> ((currentDuration - effectiveStart) / effectiveDuration)
					.toFloat()
					.coerceIn(0f..1f)
			}

			val lineBackgroundColor by animateColorAsState(
				if (isSelected)
					MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
				else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0f)
			)
			val lineScale by animateFloatAsState(
				targetValue = when {
					!isSynced -> 1f
					isEmphasised -> 1.05f
					else -> 0.98f
				},
				animationSpec = spring(stiffness = Spring.StiffnessLow)
			)
			val linePaddingY by animateDpAsState(
				targetValue = if (isEmphasised) 20.dp else 12.dp,
				animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
			)
			val lineOffsetY by animateDpAsState(
				targetValue = when {
					isActive || isSelecting || !isSynced -> 0.dp
					(index > activeIndex) -> 8.dp
					else -> (-8).dp
				},
				animationSpec = spring(stiffness = Spring.StiffnessLow)
			)
			val lineBlurRadius by animateDpAsState(
				targetValue = when {
					isSelecting -> 0.dp
					!isSynced -> 0.dp
					isActive -> 0.dp
					distance == 1 -> 1.5.dp
					distance == 2 -> 3.dp
					else -> 4.5.dp
				},
				animationSpec = spring(stiffness = Spring.StiffnessLow)
			)

			val highlight = if (isSelecting) isSelected else isActive
			val progress = when {
				isSelecting -> if (isSelected) 1f else 0f
				!isSynced -> 1f
				isActive -> line.words
					?.takeIf { it.isNotEmpty() }
					?.calculateWordProgress(line.text, currentDuration)
					?: lineProgress

				else -> 0f
			}

			LyricsScreenKaraokeText(
				text = line.text,
				progress = progress,
				isActive = highlight,
				onClick = {
					if (isSelecting) {
						val lineTextLength = line.text.length
						if (selectedIndices.isEmpty()) {
							if (lineTextLength <= maxSelectionChars) {
								onAddSelectedIndex(index)
							}
						} else {
							if (index in selectedIndices) {
								val isEndpoint =
									index == selectedIndices.first() || index == selectedIndices.last()

								if (isEndpoint) {
									onRemoveSelectedIndex(index)
								} else {
									// restart the selection at the tapped index
									// if it's in the middle of the range
									onRestartAtIndex(index)
								}
							} else {
								val minIndex = selectedIndices.minOrNull() ?: index
								val maxIndex = selectedIndices.maxOrNull() ?: index
								val isAdjacent = index == minIndex - 1 || index == maxIndex + 1
								val newChars = totalSelectedChars() + lineTextLength

								if (newChars <= maxSelectionChars) {
									// only extend the selection if the tapped line
									// is adjacent to an edge
									if (isAdjacent) {
										onAddSelectedIndex(index)
									} else {
										onRestartAtIndex(index)
									}
								}
							}
						}
					} else {
						player.seek((lineTime / duration).toFloat())
						if (player.uiState.value.isPaused) {
							player.resume()
						}
					}
				},
				modifier = Modifier
					.padding(horizontal = 32.dp, vertical = linePaddingY)
					.graphicsLayer {
						scaleX = lineScale
						scaleY = lineScale
						translationY = lineOffsetY.toPx()
					}
					.then(
						if (lineBlurRadius > 0.dp && !isSelecting && preferenceManager.lyricsBlur) {
							Modifier.blur(lineBlurRadius)
						} else Modifier
					)
					.background(lineBackgroundColor, MaterialTheme.shapes.medium)
			)
		}
		provider?.let { provider ->
			item {
				Text(
					stringResource(
						Res.string.info_lyrics_provider,
						provider.displayName
					),
					textAlign = TextAlign.Center,
					modifier = Modifier.fillMaxWidth()
				)
			}
		}
	}
}

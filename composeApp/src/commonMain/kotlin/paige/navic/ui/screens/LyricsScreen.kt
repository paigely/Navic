package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.info_lyrics_provider
import navic.composeapp.generated.resources.info_no_lyrics
import navic.composeapp.generated.resources.notice_loading_lyrics
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalMediaPlayer
import paige.navic.data.models.settings.Settings
import paige.navic.data.repositories.LyricWord
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Lyrics
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.viewmodels.LyricsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.fadeFromTop
import paige.navic.utils.rememberTrackPainter
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricsScreen(
	track: Song?,
	viewModel: LyricsViewModel = viewModel(key = track?.id) {
		LyricsViewModel(track)
	}
) {
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val state by viewModel.lyricsState.collectAsState()

	var isSelectionMode by remember { mutableStateOf(false) }
	val selectedIndices = remember { mutableStateListOf<Int>() }
	var wasPlayingBeforeSelection by remember { mutableStateOf(false) }
	var showShareSheet by remember { mutableStateOf(false) }

	val placeholder = @Composable {
		ContentUnavailable(
			modifier = Modifier.fillMaxSize(),
			icon = Icons.Outlined.Lyrics,
			label = stringResource(Res.string.info_no_lyrics)
		)
	}

	val track = track ?: return placeholder()
	val duration = track.duration

	val progressState = playerState.progress
	val currentDuration = duration * progressState.toDouble()

	val density = LocalDensity.current
	val listState = viewModel.listState

	val lyricsAutoscroll = Settings.shared.lyricsAutoscroll && !isSelectionMode

	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

	val sharedPainter = rememberTrackPainter(track.id, track.coverArtId)

	Box(modifier = Modifier.fillMaxSize()) {
		AnimatedContent(
			state,
			modifier = Modifier.fillMaxSize(),
			transitionSpec = {
				(fadeIn(
					animationSpec = effectSpec
				) + scaleIn(
					initialScale = 0.8f,
					animationSpec = spatialSpec
				)) togetherWith (fadeOut(
					animationSpec = effectSpec
				) + scaleOut(
					animationSpec = spatialSpec
				))
			},
		) { uiState ->
			when (uiState) {
				is UiState.Error -> ErrorBox(
					error = uiState,
					modifier = Modifier.wrapContentSize(),
					onRetry = { viewModel.refreshResults() }
				)

				is UiState.Loading -> LoadingScreen()
				is UiState.Success -> {
					val lyrics = uiState.data?.lines
					val provider = uiState.data?.provider
					val maxSelectionChars = 150
					fun totalSelectedChars(): Int = selectedIndices.sumOf { lyrics?.getOrNull(it)?.text?.length ?: 0 }

					if (!lyrics.isNullOrEmpty()) {
						val activeIndex = lyrics.indexOfLast { line ->
							line.time != null && currentDuration >= line.time
						}

						LaunchedEffect(activeIndex, isSelectionMode) {
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
									delay(500)
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
							Modifier.fillMaxSize().fadeFromTop(),
							state = listState,
							contentPadding = WindowInsets.statusBars.asPaddingValues()
								+ WindowInsets.systemBars.asPaddingValues()
								+ PaddingValues(vertical = 40.dp)
						) {
							itemsIndexed(lyrics) { index, line ->
								val isActive = index == activeIndex
								val isSelected = selectedIndices.contains(index)

								val lineTime = line.time ?: 0.milliseconds
								val preEmphasis = 200.milliseconds
								val nextTime = lyrics.getOrNull(index + 1)?.time ?: duration
								val lineDuration = (nextTime - lineTime).coerceAtLeast(1.milliseconds)
								val effectiveStart = lineTime - preEmphasis
								val effectiveDuration = lineDuration + preEmphasis

								val lineProgress = when {
									currentDuration < effectiveStart -> 0f
									currentDuration >= effectiveStart + effectiveDuration -> 1f
									else -> ((currentDuration - effectiveStart) / effectiveDuration).toFloat().coerceIn(0f, 1f)
								}

								val padding by animateDpAsState(
									if ((isActive && !isSelectionMode) || (isSelectionMode && isSelected)) 20.dp else 12.dp,
									animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
								)

								val targetColor = if (isSelected)
									MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
									else Color.Transparent
								val animatedColor by animateColorAsState(
									targetColor
								)
								val targetScale = if (isActive && !isSelectionMode) 1.06f else 1f
								val animatedScale by animateFloatAsState(
									targetValue = targetScale,
									animationSpec = spring(stiffness = Spring.StiffnessLow)
								)

								val highlight = if (isSelectionMode) isSelected else isActive
								val progress = if (isSelectionMode && isSelected) {
									1.0f
								} else if (!isSelectionMode && isActive) {
									if (line.words.isNullOrEmpty()) {
										lineProgress
									} else {
										calculateWordProgress(line.words, line.text, currentDuration)
									}
								} else {
									0f
								}

								KaraokeText(
									text = line.text,
									progress = progress,
									isActive = highlight,
									onClick = {
										if (isSelectionMode) {
											if (selectedIndices.isEmpty()) {
												val chars = line.text.length
												if (chars <= maxSelectionChars) selectedIndices.add(index)
											} else {
												if (selectedIndices.contains(index)) {
													if (index == selectedIndices.first() || index == selectedIndices.last()) {
														selectedIndices.remove(index)
													} else {
														selectedIndices.clear()
														selectedIndices.add(index)
													}
												} else {
													val minIndex = selectedIndices.minOrNull() ?: index
													val maxIndex = selectedIndices.maxOrNull() ?: index
													val newChars = totalSelectedChars() + line.text.length
													if (newChars <= maxSelectionChars) {
														if (index == minIndex - 1 || index == maxIndex + 1) {
															selectedIndices.add(index)
														} else {
															selectedIndices.clear()
															selectedIndices.add(index)
														}
													}
												}
											}
										} else {
											player.seek((lineTime / duration).toFloat())
											if (playerState.isPaused) {
												player.resume()
											}
										}
									},
									modifier = Modifier
										.padding(horizontal = 32.dp, vertical = padding)
										.scale(animatedScale)
										.background(animatedColor, MaterialTheme.shapes.medium)
										.padding(if (isSelected) 8.dp else 0.dp)
										.then(
											if (index == 0) {
												Modifier.padding(top = 16.dp)
											} else {
												Modifier
											}
										)
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
					} else {
						placeholder()
					}
				}
			}
		}
		Row(
			modifier = Modifier
				.align(Alignment.BottomStart)
				.padding(12.dp),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			IconButton(
				modifier = Modifier
					.size(48.dp)
					.background(
						color = if (isSelectionMode) MaterialTheme.colorScheme.primary else Color.Black.copy(
							alpha = 0.2f
						),
						shape = MaterialTheme.shapes.medium
					),
				onClick = {
					if (isSelectionMode) {
						isSelectionMode = false
						selectedIndices.clear()
						if (wasPlayingBeforeSelection) {
							player.resume()
						}
					} else {
						wasPlayingBeforeSelection = !playerState.isPaused
						player.pause()
						isSelectionMode = true
					}
				}) {
				Icon(
					imageVector = if (isSelectionMode) Icons.Outlined.Close else Icons.Outlined.Share,
					contentDescription = null,
					tint = if (isSelectionMode) MaterialTheme.colorScheme.onPrimary else Color.White
				)
			}
			AnimatedVisibility(
				visible = isSelectionMode && selectedIndices.isNotEmpty(),
				enter = scaleIn() + fadeIn(),
				exit = scaleOut() + fadeOut()
			) {
				IconButton(
					modifier = Modifier
						.size(48.dp)
						.background(
							color = MaterialTheme.colorScheme.onPrimary,
							shape = MaterialTheme.shapes.medium
						),
					onClick = { showShareSheet = true }
				) {
					Icon(
						imageVector = Icons.Outlined.Check,
						contentDescription = stringResource(Res.string.action_share)
					)
				}
			}
		}

		if (showShareSheet) {
			val lyricsList = (state as? UiState.Success)?.data?.lines
				?.map { line ->
					(line.time?.inWholeMilliseconds ?: 0L) to line.text
				}

			if (lyricsList != null) {
				val sortedIndices = selectedIndices.sorted()
				val stringsToShare = sortedIndices.mapNotNull { index ->
					lyricsList.getOrNull(index)?.second
				}

				LyricsShareSheet(
					track = track,
					selectedLyrics = stringsToShare,
					sharedPainter = sharedPainter,
					onDismiss = { showShareSheet = false },
					onShare = {
						showShareSheet = false
						isSelectionMode = false
						selectedIndices.clear()
					}
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LoadingScreen() {
	Column(
		Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
	) {
		ContainedLoadingIndicator(
			Modifier.size(80.dp)
		)
		Text(
			stringResource(Res.string.notice_loading_lyrics),
			textAlign = TextAlign.Center,
			fontWeight = FontWeight(600),
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun KaraokeText(
	text: String,
	progress: Float,
	isActive: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

	val smoothProgress by animateFloatAsState(
		targetValue = progress,
		animationSpec = spring(stiffness = Spring.StiffnessLow, visibilityThreshold = 0.001f)
	)

	val lyricsBeatByBeat = Settings.shared.lyricsBeatByBeat

	val inactiveAlpha by animateFloatAsState(
		if (isActive) 1f else 0.35f,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)

	Box(
		modifier = modifier.clickable {
			onClick()
		}
	) {
		Text(
			text = text,
			fontSize = 32.sp,
			fontWeight = FontWeight(600),
			style = MaterialTheme.typography.headlineLargeEmphasized,
			modifier = Modifier.alpha(inactiveAlpha * 0.35f),
			onTextLayout = { textLayoutResult = it }
		)

		if (isActive) {
			Text(
				text = text,
				fontSize = 32.sp,
				fontWeight = FontWeight(600),
				style = MaterialTheme.typography.headlineLargeEmphasized,
				modifier = if (lyricsBeatByBeat) Modifier.graphicsLayer(
					compositingStrategy = CompositingStrategy.Offscreen
				).drawWithCache {
					onDrawWithContent {
						val layout = textLayoutResult ?: return@onDrawWithContent
						drawContent()

						val totalWidth = (0 until layout.lineCount).sumOf {
							(layout.getLineRight(it) - layout.getLineLeft(it)).toDouble()
						}.toFloat()

						val feather = 30f
						val adjustedTotalWidth = totalWidth + (feather * 2)
						val currentPixelTarget = (adjustedTotalWidth * smoothProgress) - feather

						var accumulatedWidth = 0f

						for (i in 0 until layout.lineCount) {
							val lineLeft = layout.getLineLeft(i)
							val lineRight = layout.getLineRight(i)
							val lineWidth = lineRight - lineLeft

							if (lineWidth <= 0f) continue

							val lineTop = layout.getLineTop(i)
							val lineBottom = layout.getLineBottom(i)

							val lineStartOffset = layout.getLineStart(i)
							val direction = layout.getBidiRunDirection(lineStartOffset)
							val isRtl = direction == ResolvedTextDirection.Rtl

							val startOffFadeIn = currentPixelTarget - accumulatedWidth - feather
							val endOfFadeIn = currentPixelTarget - accumulatedWidth + feather

							val startX = if (isRtl) lineRight else lineLeft
							val endX = if (isRtl) lineLeft else lineRight

							val brush = Brush.linearGradient(
								0.0f to Color.White,
								(startOffFadeIn / lineWidth).coerceIn(0f, 1f) to Color.White,
								(endOfFadeIn / lineWidth).coerceIn(0f, 1f) to Color.Transparent,
								1.0f to Color.Transparent,
								start = Offset(startX, 0f),
								end = Offset(endX, 0f)
							)

							drawRect(
								brush = brush,
								topLeft = Offset(lineLeft, lineTop),
								size = Size(lineWidth, lineBottom - lineTop),
								blendMode = BlendMode.Modulate
							)

							accumulatedWidth += lineWidth
						}
					}
				} else Modifier
			)
		}
	}
}

private fun calculateWordProgress(
	words: List<LyricWord>,
	fullText: String,
	currentDuration: Duration
): Float {
	if (words.isEmpty() || fullText.isEmpty()) return 0f

	val currentMs = currentDuration.inWholeMilliseconds
	val totalChars = fullText.length.toFloat()

	if (currentMs < words.first().time.inWholeMilliseconds) return 0f

	var currentCharacterIndex = 0

	for (i in words.indices) {
		val word = words[i]
		val wordStartMs = word.time.inWholeMilliseconds
		val wordEndMs = wordStartMs + word.duration.inWholeMilliseconds

		val wordIndexInString = fullText.indexOf(word.text, startIndex = currentCharacterIndex, ignoreCase = true)

		if (wordIndexInString == -1) {
			continue
		}

		if (currentMs in wordStartMs until wordEndMs) {
			val wordProgress = (currentMs - wordStartMs).toFloat() / word.duration.inWholeMilliseconds.coerceAtLeast(1)
			val charProgressWithinWord = word.text.length * wordProgress

			return (wordIndexInString + charProgressWithinWord) / totalChars
		}

		if (currentMs < wordStartMs) {
			return wordIndexInString / totalChars
		}

		currentCharacterIndex = wordIndexInString + word.text.length
	}

	return 1f
}
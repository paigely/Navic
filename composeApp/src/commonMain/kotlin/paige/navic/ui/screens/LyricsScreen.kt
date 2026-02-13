package paige.navic.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.notice_loading_lyrics
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalMediaPlayer
import paige.navic.data.models.Settings
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.BlendBackground
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.viewmodels.LyricsViewModel
import paige.navic.utils.UiState
import paige.navic.utils.rememberTrackPainter
import paige.subsonic.api.models.Track
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricsScreen(
	track: Track?,
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
		Column(
			modifier = Modifier.fillMaxSize(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Text(
				"No lyrics",
				style = MaterialTheme.typography.headlineMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.alpha(.5f)
			)
		}
	}

	val track = track ?: return placeholder()
	val duration = track.duration?.toDuration(DurationUnit.SECONDS) ?: return placeholder()

	val progressState = playerState.progress
	val currentDuration = duration * progressState.toDouble()

	val density = LocalDensity.current
	val listState = rememberLazyListState()

	val lyricsAutoscroll = Settings.shared.lyricsAutoscroll && !isSelectionMode

	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

	val sharedPainter = rememberTrackPainter(track.id, track.coverArt)

	Box(modifier = Modifier.fillMaxSize()) {
		if (Settings.shared.animatePlayerBackground) {
			BlendBackground(
				painter = sharedPainter,
				isPaused = playerState.isPaused
			)
		}
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
					modifier = Modifier.wrapContentSize()
				)

				is UiState.Loading -> LoadingScreen()
				is UiState.Success -> {
					val lyrics = uiState.data
					val maxSelectionChars = 150
					fun totalSelectedChars(): Int = selectedIndices.sumOf { lyrics?.getOrNull(it)?.second?.length ?: 0 }

					if (!lyrics.isNullOrEmpty()) {
						val activeIndex = lyrics.indexOfLast { (time, _) ->
							currentDuration >= time
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
										animationSpec = tween(
											durationMillis = 450,
											easing = FastOutSlowInEasing
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
							Modifier.fillMaxSize(),
							state = listState,
							contentPadding = WindowInsets.statusBars.asPaddingValues()
								+ WindowInsets.systemBars.asPaddingValues()
						) {
							itemsIndexed(lyrics) { index, (startTime, text) ->
								val isActive = index == activeIndex
								val isSelected = selectedIndices.contains(index)

								val lineProgress = when {
									index < activeIndex -> 1f
									index > activeIndex -> 0f
									else -> {
										val nextTime =
											lyrics.getOrNull(index + 1)?.first ?: duration
										val lineDuration = nextTime - startTime
										if (lineDuration > Duration.ZERO) {
											((currentDuration - startTime) / lineDuration).toFloat()
												.coerceIn(0f, 1f)
										} else 1f
									}
								}

								val padding by animateDpAsState(
									if ((isActive && !isSelectionMode) || (isSelectionMode && isSelected)) 20.dp else 12.dp,
									animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
								)

								val targetColor = if (isSelected)
									MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
									else Color.Transparent
								val animatedColor by androidx.compose.animation.animateColorAsState(
									targetColor
								)

								val highlight = if (isSelectionMode) isSelected else isActive
								val progress = if (isSelectionMode && isSelected) {
									1.1f
								} else if (!isSelectionMode && isActive) {
									lineProgress
								} else {
									0f
								}

								KaraokeText(
									text = text,
									progress = progress,
									isActive = highlight,
									onClick = {
										if (isSelectionMode) {
											if (selectedIndices.isEmpty()) {
												val chars = text.length
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
													val newChars = totalSelectedChars() + text.length
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
											player.seek((startTime / duration).toFloat())
											if (playerState.isPaused) {
												player.resume()
											}
										}
									},
									modifier = Modifier
										.padding(horizontal = 32.dp, vertical = padding)
										.background(animatedColor, RoundedCornerShape(12.dp))
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
						shape = RoundedCornerShape(12.dp)
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
							shape = RoundedCornerShape(12.dp)
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
			val lyricsList = (state as? UiState.Success)?.data
				?.map { (duration, text) ->
					duration.inWholeMilliseconds to text
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

						val currentPixelTarget = totalWidth * smoothProgress

						val feather = 30f
						var accumulatedWidth = 0f

						for (i in 0 until layout.lineCount) {
							val lineLeft = layout.getLineLeft(i)
							val lineRight = layout.getLineRight(i)
							val lineWidth = lineRight - lineLeft
							val lineTop = layout.getLineTop(i)
							val lineBottom = layout.getLineBottom(i)

							val startOffFadeIn = currentPixelTarget - accumulatedWidth - feather
							val endOfFadeIn = currentPixelTarget - accumulatedWidth + feather

							val brush = Brush.linearGradient(
								0.0f to Color.White,
								(startOffFadeIn / lineWidth).coerceIn(0f, 1f) to Color.White,
								(endOfFadeIn / lineWidth).coerceIn(0f, 1f) to Color.Transparent,
								1.0f to Color.Transparent,
								start = Offset(lineLeft, 0f),
								end = Offset(lineRight, 0f)
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

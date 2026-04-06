package paige.navic.ui.screens.lyrics

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
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.info_lyrics_provider
import navic.composeapp.generated.resources.info_no_lyrics
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Lyrics
import paige.navic.icons.outlined.Share
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.KeepScreenOn
import paige.navic.ui.screens.lyrics.components.LyricsScreenKaraokeText
import paige.navic.ui.screens.lyrics.components.LyricsScreenLoadingView
import paige.navic.ui.screens.lyrics.dialogs.LyricsShareSheet
import paige.navic.ui.screens.lyrics.viewmodels.LyricsScreenViewModel
import paige.navic.utils.UiState
import paige.navic.utils.calculateWordProgress
import paige.navic.utils.fadeFromTop
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricsScreen(
	track: DomainSong?
) {
	val viewModel = koinViewModel<LyricsScreenViewModel>(
		key = track?.id,
		parameters = { parametersOf(track) }
	)
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val state by viewModel.lyricsState.collectAsState()

	if (Settings.shared.lyricsKeepAlive) {
		KeepScreenOn()
	}

	var isSelectionMode by rememberSaveable { mutableStateOf(false) }
	val selectedIndices = rememberSaveable { mutableStateListOf<Int>() }
	var wasPlayingBeforeSelection by rememberSaveable { mutableStateOf(false) }
	var showShareSheet by rememberSaveable { mutableStateOf(false) }

	val contentColor = MaterialTheme.colorScheme.onSurface


	val placeholder = @Composable {
		ContentUnavailable(
			modifier = Modifier.fillMaxSize(),
			icon = Icons.Outlined.Lyrics,
			color = contentColor,
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

				is UiState.Loading -> LyricsScreenLoadingView()
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
										line.words.calculateWordProgress(line.text, currentDuration)
									}
								} else {
									0f
								}

								LyricsScreenKaraokeText(
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
				}.toImmutableList()

				LyricsShareSheet(
					track = track,
					selectedLyrics = stringsToShare,
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

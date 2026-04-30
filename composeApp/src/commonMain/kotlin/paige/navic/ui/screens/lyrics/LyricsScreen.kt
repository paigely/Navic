package paige.navic.ui.screens.lyrics

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_navigate_back
import navic.composeapp.generated.resources.action_share_lyrics
import navic.composeapp.generated.resources.count_lines
import navic.composeapp.generated.resources.info_lyrics_provider
import navic.composeapp.generated.resources.info_no_lyrics
import navic.composeapp.generated.resources.title_select_lyrics
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.ToolbarPosition
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ArrowBack
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.KeyboardArrowDown
import paige.navic.icons.outlined.Lyrics
import paige.navic.icons.outlined.Share
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.KeepScreenOn
import paige.navic.ui.components.layouts.SheetScaffold
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.components.toolbars.SheetToolbar
import paige.navic.ui.screens.lyrics.components.LyricsScreenKaraokeText
import paige.navic.ui.screens.lyrics.components.LyricsScreenLoadingView
import paige.navic.ui.screens.lyrics.dialogs.LyricsShareSheet
import paige.navic.ui.screens.lyrics.viewmodels.LyricsScreenViewModel
import paige.navic.utils.UiState
import paige.navic.utils.calculateWordProgress
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricsScreen(
	song: DomainSong?
) {
	val backStack = LocalNavStack.current
	val viewModel = koinViewModel<LyricsScreenViewModel>(
		key = song?.id,
		parameters = { parametersOf(song) }
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

	val song = song ?: return placeholder()
	val duration = song.duration

	val progressState = playerState.progress
	val currentDuration = duration * progressState.toDouble()

	val density = LocalDensity.current
	val listState = viewModel.listState

	val lyricsAutoscroll = Settings.shared.lyricsAutoscroll && !isSelectionMode

	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

	val toggleSelectionMode = {
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
	}

	SheetScaffold(
		toolbar = { windowInsets ->
			SheetToolbar(
				windowInsets = windowInsets,
				navigationIcon = {
					TopBarButton(
						onClick = {
							if (!isSelectionMode) {
								backStack.remove(Screen.Lyrics)
							} else {
								toggleSelectionMode()
							}
						},
						content = {
							Icon(
								imageVector = if (!isSelectionMode)
									Icons.Outlined.KeyboardArrowDown
								else Icons.Outlined.ArrowBack,
								contentDescription = stringResource(Res.string.action_navigate_back)
							)
						}
					)
					NavigationBackHandler(
						state = rememberNavigationEventState(NavigationEventInfo.None),
						isBackEnabled = isSelectionMode,
						onBackCompleted = toggleSelectionMode
					)
				},
				actions = {
					TopBarButton(
						enabled = !isSelectionMode || selectedIndices.isNotEmpty(),
						onClick = {
							if (isSelectionMode) {
								showShareSheet = true
							} else {
								toggleSelectionMode()
							}
						}
					) {
						Icon(
							imageVector = if (!isSelectionMode)
								Icons.Outlined.Share
							else Icons.Outlined.Check,
							contentDescription = stringResource(Res.string.action_share_lyrics),
							modifier = Modifier.size(26.dp)
						)
					}
				},
				title = {
					if (isSelectionMode) {
						Column {
							Text(
								stringResource(Res.string.title_select_lyrics),
								fontWeight = FontWeight.SemiBold
							)
							Text(pluralStringResource(
								Res.plurals.count_lines,
								selectedIndices.count(),
								selectedIndices.count()
							))
						}
					}
				}
			)
		},
		toolbarPosition = ToolbarPosition.Top
	) { contentPadding ->
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
					val data = uiState.data
					val lyrics = data?.lines
					val isSynced = data?.isSynced == true
					val provider = data?.provider
					val maxSelectionChars = 150
					fun totalSelectedChars(): Int =
						selectedIndices.sumOf { lyrics?.getOrNull(it)?.text?.length ?: 0 }

					if (!lyrics.isNullOrEmpty()) {
						val activeIndex = if (isSynced) {
							lyrics.indexOfLast { line ->
								line.time != null && currentDuration >= line.time
							}
						} else -1

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
							Modifier.fillMaxSize(),
							state = listState,
							contentPadding = contentPadding
						) {
							itemsIndexed(lyrics) { index, line ->
								val isActive = if (isSynced) index == activeIndex else true
								val isSelected = selectedIndices.contains(index)
								val distance = abs(index - activeIndex)

								val blurRadius by animateDpAsState(
									targetValue = when {
										isSelectionMode -> 0.dp
										!isSynced -> 0.dp
										isActive -> 0.dp
										distance == 1 -> 1.5.dp
										distance == 2 -> 3.dp
										else -> 4.5.dp
									},
									animationSpec = spring(stiffness = Spring.StiffnessLow)
								)

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
									else -> ((currentDuration - effectiveStart) / effectiveDuration).toFloat()
										.coerceIn(0f, 1f)
								}

								val padding by animateDpAsState(
									if ((isActive && !isSelectionMode && isSynced) || (isSelectionMode && isSelected)) 20.dp else 12.dp,
									animationSpec = MaterialTheme.motionScheme.slowSpatialSpec()
								)

								val targetColor = if (isSelected)
									MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
								else Color.Transparent
								val animatedColor by animateColorAsState(
									targetColor
								)
								val targetScale =
									if (isActive && !isSelectionMode && isSynced) 1.05f else if (!isSynced) 1.0f else 0.98f
								val animatedScale by animateFloatAsState(
									targetValue = targetScale,
									animationSpec = spring(stiffness = Spring.StiffnessLow)
								)

								val targetOffsetY =
									if (isActive || isSelectionMode || !isSynced) 0.dp else if (index > activeIndex) 8.dp else (-8).dp
								val animatedOffsetY by animateDpAsState(
									targetValue = targetOffsetY,
									animationSpec = spring(stiffness = Spring.StiffnessLow)
								)

								val highlight = if (isSelectionMode) isSelected else isActive
								val progress = if (isSelectionMode && isSelected) {
									1.0f
								} else if (!isSynced) {
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
												if (chars <= maxSelectionChars) selectedIndices.add(
													index
												)
											} else {
												if (selectedIndices.contains(index)) {
													if (index == selectedIndices.first() || index == selectedIndices.last()) {
														selectedIndices.remove(index)
													} else {
														selectedIndices.clear()
														selectedIndices.add(index)
													}
												} else {
													val minIndex =
														selectedIndices.minOrNull() ?: index
													val maxIndex =
														selectedIndices.maxOrNull() ?: index
													val newChars =
														totalSelectedChars() + line.text.length
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
										.graphicsLayer {
											scaleX = animatedScale
											scaleY = animatedScale
											translationY = animatedOffsetY.toPx()
										}
										.then(
											if (blurRadius > 0.dp && !isSelectionMode && Settings.shared.lyricsBlur) {
												Modifier.blur(blurRadius)
											} else Modifier
										)
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
					song = song,
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

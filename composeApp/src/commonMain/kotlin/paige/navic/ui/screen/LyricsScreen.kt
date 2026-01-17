package paige.navic.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import paige.navic.LocalMediaPlayer
import paige.navic.ui.component.common.ErrorBox
import paige.navic.ui.viewmodel.LyricsViewModel
import paige.navic.util.UiState
import paige.subsonic.api.model.Track
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
	val state by viewModel.lyricsState.collectAsState()

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

	val progressState by player.progress
	val currentDuration = duration * progressState.toDouble()

	AnimatedContent(
		state,
		modifier = Modifier.fillMaxSize()
	) { uiState ->
		when (uiState) {
			is UiState.Error -> ErrorBox(uiState)
			is UiState.Success -> {
				val lyrics = uiState.data
				if (!lyrics.isNullOrEmpty()) {
					val activeIndex = lyrics.indexOfLast { (time, _) ->
						currentDuration >= time
					}

					LazyColumn(Modifier.fillMaxSize()) {
						itemsIndexed(lyrics) { index, (startTime, text) ->
							val isActive = index == activeIndex

							val lineProgress = when {
								index < activeIndex -> 1f
								index > activeIndex -> 0f
								else -> {
									val nextTime = lyrics.getOrNull(index + 1)?.first ?: duration
									val lineDuration = nextTime - startTime
									if (lineDuration > Duration.ZERO) {
										((currentDuration - startTime) / lineDuration).toFloat()
											.coerceIn(0f, 1f)
									} else 1f
								}
							}

							val padding by animateDpAsState(
								if (isActive) 20.dp else 12.dp, label = "padding",
								animationSpec = spring(
									dampingRatio = Spring.DampingRatioMediumBouncy,
									stiffness = Spring.StiffnessLow
								),
							)

							KaraokeText(
								text = text,
								progress = lineProgress,
								isActive = isActive,
								onClick = {
									player.seek((startTime / duration).toFloat())
									if (player.isPaused.value) {
										player.resume()
									}
								},
								modifier = Modifier.padding(
									horizontal = 32.dp,
									vertical = padding
								).then(
									if (index == 0) {
										Modifier.padding(top = 16.dp)
									} else { Modifier }
								)
							)
						}
					}
				} else {
					placeholder()
				}
			}
			else -> placeholder()
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun KaraokeText(
	text: String,
	progress: Float,
	isActive: Boolean,
	onClick: () -> Unit,
	modifier: Modifier = Modifier
) {
	var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

	val smoothProgress by animateFloatAsState(
		targetValue = progress,
		animationSpec = spring(stiffness = Spring.StiffnessLow, visibilityThreshold = 0.001f),
		label = "smoothProgress"
	)

	val inactiveAlpha by animateFloatAsState(if (isActive) 1f else 0.35f, label = "alpha")

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
				modifier = Modifier.graphicsLayer(
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
				}
			)
		}
	}
}

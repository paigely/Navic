package paige.navic.ui.component.layout

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonColors
import androidx.compose.material3.ToggleButtonShapes
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import ir.mahozad.multiplatform.wavyslider.material3.WaveHeight
import ir.mahozad.multiplatform.wavyslider.material3.WavySlider
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_more
import navic.composeapp.generated.resources.action_shuffle
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.more_vert
import navic.composeapp.generated.resources.pause
import navic.composeapp.generated.resources.play_arrow
import navic.composeapp.generated.resources.playlist_play
import navic.composeapp.generated.resources.shuffle
import navic.composeapp.generated.resources.skip_next
import navic.composeapp.generated.resources.skip_previous
import navic.composeapp.generated.resources.unstar
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.data.model.Settings
import paige.navic.data.session.SessionManager
import paige.navic.shared.Ctx
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.shared.PlayerUiState
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.component.common.Marquee
import paige.navic.ui.screen.LyricsScreen
import paige.navic.util.toHHMMSS
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Playlist
import kotlin.time.Duration.Companion.seconds

object MediaBarDefaults {
	val height = 117.9.dp
	val heightNoSeekbar = 75.dp
	val collapsedArtSize = 55.dp
}

private class MediaBarScope(
	val coverUri: String?,
	val player: MediaPlayerViewModel,
	val playerState: PlayerUiState,
	val ctx: Ctx
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MediaBar() {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val coverUri = remember(playerState.currentTrack?.coverArt) {
		SessionManager.api.getCoverArtUrl(
			playerState.currentTrack?.coverArt,
			auth = true
		)
	}

	var mediaBarTop by remember { mutableFloatStateOf(0f) }
	var initialTop by remember { mutableFloatStateOf(0f) }

	val progress by remember(mediaBarTop, initialTop) {
		derivedStateOf {
			if (initialTop == 0f) 0f else ((initialTop - mediaBarTop) / initialTop).coerceIn(0f, 1f)
		}
	}

	val isFullyExpanded by remember(progress) {
		derivedStateOf { progress >= 1f }
	}

	val scope = remember(coverUri, player, playerState, ctx) {
		MediaBarScope(coverUri, player, playerState, ctx)
	}

	BoxWithConstraints(
		Modifier
			.fillMaxHeight()
			.onGloballyPositioned { coordinates ->
				val topPx = coordinates.positionInWindow().y
				mediaBarTop = topPx
				if (initialTop == 0f && topPx > 0) initialTop = topPx
			}
	) {
		val maxWidth = maxWidth
		val artStartSize = MediaBarDefaults.collapsedArtSize
		val artEndSize by animateDpAsState(
			targetValue = if (playerState.isPaused) 260.dp else 300.dp,
			animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
			label = "AlbumArtSizeAnimation"
		)

		val currentArtSize = lerp(artStartSize, artEndSize, progress)

		val startX = 15.dp
		val startY = 15.dp

		val endX = (maxWidth - artEndSize) / 2
		val endY = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 100.dp

		val currentArtX = lerp(startX, endX, progress)
		val currentArtY = lerp(startY, endY, progress)

		val currentCornerRadius = lerp(12.dp, 18.dp, progress)

		val collapsedAlpha by remember(progress) {
			derivedStateOf { (1f - (progress * 3.3f)).coerceIn(0f, 1f) }
		}
		val expandedAlpha by remember(progress) {
			derivedStateOf { ((progress - 0.5f) * 2f).coerceIn(0f, 1f) }
		}

		if (progress > 0f) {
			scope.ExpandedBackground(progress)
		}

		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(MediaBarDefaults.height)
				.graphicsLayer {
					alpha = collapsedAlpha
					translationY = progress * -50f
				}
		) {
			if (collapsedAlpha > 0f) {
				scope.CollapsedContent()
			}
		}
		Box(
			modifier = Modifier
				.fillMaxSize()
				.statusBarsPadding()
				.graphicsLayer {
					alpha = expandedAlpha
					translationY = (1f - expandedAlpha) * 100f
				}
		) {
			if (expandedAlpha > 0f) {
				scope.ExpandedContent(
					progress = progress,
					artSize = artEndSize,
					topPadding = 100.dp,
					showArt = isFullyExpanded
				)
			}
		}
		if (!isFullyExpanded) {
			Surface(
				modifier = Modifier
					.offset(currentArtX, currentArtY)
					.size(currentArtSize),
				shape = ContinuousRoundedRectangle(currentCornerRadius),
				color = MaterialTheme.colorScheme.surfaceVariant,
				shadowElevation = lerp(0.dp, 10.dp, progress)
			) {
				scope.AlbumArt()
			}
		}
	}
}
@Composable
private fun MediaBarScope.ExpandedBackground(progress: Float) {
	Box(Modifier.fillMaxSize()) {
		AlbumArt(
			modifier = Modifier
				.fillMaxSize()
				.blur(lerp(0.dp, 150.dp, progress), edgeTreatment = BlurredEdgeTreatment.Rectangle)
				.graphicsLayer { alpha = lerp(0f, 0.75f, progress) }
				.drawWithContent {
					drawContent()
					drawRect(
						brush = Brush.verticalGradient(
							0f to Color.Black,
							.6f to Color.Black,
							.7f to Color.Transparent,
						),
						blendMode = BlendMode.DstIn
					)
				}
		)
	}
}

@Composable
private fun MediaBarScope.CollapsedContent() {
	val alwaysShowSeekbar = Settings.shared.alwaysShowSeekbar
	Column(
		Modifier.height(if (alwaysShowSeekbar) MediaBarDefaults.height else MediaBarDefaults.heightNoSeekbar)
	) {
		Row(
			modifier = Modifier
				.padding(top = 15.dp, start = 15.dp, end = 15.dp)
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Spacer(Modifier.size(55.dp))
			Spacer(Modifier.width(10.dp))

			Info(modifier = Modifier.weight(1f))

			Controls(expanded = false, progress = 0f)
		}
		if (alwaysShowSeekbar) {
			ProgressBar(expanded = false)
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.ExpandedContent(
	progress: Float,
	artSize: Dp,
	topPadding: Dp,
	showArt: Boolean
) {
	val pagerState = rememberPagerState(pageCount = { 2 })
	val currentIndex = playerState.currentIndex
	val currentTrack = playerState.tracks?.tracks?.getOrNull(currentIndex)

	Box(Modifier.fillMaxSize()) {
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize()
		) { page ->
			when (page) {
				0 -> PlayerView(progress, artSize, topPadding, showArt)
				1 -> LyricsScreen(currentTrack)
			}
		}

		Row(
			Modifier
				.wrapContentHeight()
				.fillMaxWidth()
				.align(Alignment.BottomCenter)
				.padding(bottom = 8.dp),
			horizontalArrangement = Arrangement.Center
		) {
			repeat(pagerState.pageCount) { iteration ->
				val color by animateColorAsState(
					if (pagerState.currentPage == iteration)
						MaterialTheme.colorScheme.onSurface
					else MaterialTheme.colorScheme.onSurface.copy(alpha = .25f)
				)
				Box(
					modifier = Modifier
						.padding(4.dp)
						.clip(CircleShape)
						.background(color)
						.size(8.dp)
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.PlayerView(
	progress: Float,
	artSize: Dp,
	topPadding: Dp,
	showArt: Boolean
) {
	var moreShown by remember { mutableStateOf(false) }

	Column(
		Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Top
	) {
		Spacer(Modifier.height(topPadding))
		Surface(
			modifier = Modifier
				.size(artSize)
				.graphicsLayer { alpha = if (showArt) 1f else 0f },
			shape = ContinuousRoundedRectangle(18.dp),
			color = MaterialTheme.colorScheme.surfaceVariant,
			shadowElevation = 10.dp
		) {
			AlbumArt()
		}

		Column(
			Modifier
				.padding(horizontal = 15.dp)
				.padding(top = 24.dp)
		) {
			Row(
				Modifier.padding(horizontal = 15.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Info(modifier = Modifier.weight(1f))
				Box {
					IconButton(onClick = { moreShown = true }) {
						Icon(
							vectorResource(Res.drawable.more_vert),
							contentDescription = stringResource(Res.string.action_more)
						)
					}
					Dropdown(
						expanded = moreShown,
						onDismissRequest = { moreShown = false }
					) {
						DropdownItem(
							leadingIcon = Res.drawable.playlist_play,
							text = Res.string.action_add_to_playlist
						)
						DropdownItem(
							leadingIcon = Res.drawable.unstar,
							text = Res.string.action_star
						)
						DropdownItem(
							leadingIcon = Res.drawable.shuffle,
							text = Res.string.action_shuffle,
							onClick = {
								moreShown = false
								playerState.tracks?.let {
									when (it) {
										is Album -> player.play(it.copy(song = it.song?.shuffled()), 0)
										is Playlist -> player.play(it.copy(entry = it.entry?.shuffled()), 0)
									}
								}
							}
						)
					}
				}
			}
			Spacer(Modifier.height(30.dp))
			ProgressBar(expanded = true)
		}

		Spacer(Modifier.height(30.dp))
		Controls(expanded = true, progress = progress)
	}
}

@Composable
private fun MediaBarScope.AlbumArt(
	modifier: Modifier = Modifier
) {
	val uriHandler = LocalUriHandler.current
	AsyncImage(
		modifier = modifier
			.clickable {
				coverUri?.let { uri ->
					uriHandler.openUri(uri)
				}
			},
		model = coverUri,
		contentDescription = playerState.currentTrack?.title,
		contentScale = ContentScale.Crop,
		filterQuality = FilterQuality.High
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.Info(modifier: Modifier = Modifier) {
	val currentIndex = playerState.currentIndex
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.Center
	) {
		Marquee {
			Text(
				playerState.tracks?.tracks?.getOrNull(currentIndex)?.title.orEmpty(),
				fontWeight = FontWeight(600),
				maxLines = 1
			)
		}
		Text(
			playerState.tracks?.tracks?.getOrNull(currentIndex)?.artist.orEmpty(),
			style = MaterialTheme.typography.titleSmall,
			maxLines = 1
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.Controls(expanded: Boolean, progress: Float) {
	val paused = playerState.isPaused
	val size = if(expanded) 40.dp else 32.dp

	val contentPadding = if (!expanded) PaddingValues(horizontal = 4.dp) else ButtonDefaults.contentPaddingFor(60.dp)
	val shapes = ToggleButtonShapes(
		shape = ContinuousRoundedRectangle(16.dp),
		pressedShape = ContinuousRoundedRectangle(12.dp),
		checkedShape = ContinuousRoundedRectangle(12.dp)
	)

	val modifier = Modifier.size(size)
	val enabled = playerState.tracks != null
	val colors = ToggleButtonColors(
		containerColor = if (expanded) MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp) else Color.Transparent,
		contentColor = MaterialTheme.colorScheme.onSurface,
		disabledContainerColor = Color.Transparent,
		disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
		checkedContainerColor = MaterialTheme.colorScheme.primary,
		checkedContentColor = MaterialTheme.colorScheme.onPrimary
	)

	Row(
		modifier = if (expanded) {
			Modifier
				.clip(ContinuousCapsule)
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.padding(8.dp)
				.clip(ContinuousCapsule)
		} else {
			Modifier
		},
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		if (expanded) {
			ToggleButton(
				enabled = enabled,
				checked = false,
				contentPadding = contentPadding,
				shapes = shapes,
				colors = colors,
				onCheckedChange = {
					ctx.clickSound()
					player.previous()
				},
				content = { Icon(vectorResource(Res.drawable.skip_previous), null, modifier) }
			)
		}
		ToggleButton(
			enabled = enabled,
			checked = !paused,
			contentPadding = contentPadding,
			shapes = shapes,
			colors = colors,
			onCheckedChange = {
				ctx.clickSound()
				if (paused) player.resume() else player.pause()
			},
			content = {
				Icon(vectorResource(if (paused) Res.drawable.play_arrow else Res.drawable.pause), null, modifier)
			}
		)
		ToggleButton(
			enabled = enabled,
			checked = false,
			contentPadding = contentPadding,
			shapes = shapes,
			colors = colors,
			onCheckedChange = {
				ctx.clickSound()
				player.next()
			},
			content = { Icon(vectorResource(Res.drawable.skip_next), null, modifier) }
		)
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.ProgressBar(expanded: Boolean) {
	val interactionSource = remember { MutableInteractionSource() }
	val progress = playerState.progress
	val paused = playerState.isPaused
	var dragProgress by remember { mutableFloatStateOf(progress) }
	var isDragging by remember { mutableStateOf(false) }
	val shownProgress = if (isDragging) dragProgress else progress

	val waveHeight by animateDpAsState(
		if (paused) 0.dp else SliderDefaults.WaveHeight,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val thumbColor by animateColorAsState(
		if (expanded) MaterialTheme.colorScheme.onSurface else Color.Unspecified
	)

	val inactiveTrackColor = if (expanded) MaterialTheme.colorScheme.onSurface.copy(alpha = .25f) else Color.Unspecified
	val activeTickColor = if (expanded) MaterialTheme.colorScheme.onSurface else Color.Unspecified

	val colors = SliderDefaults.colors(
		thumbColor = thumbColor,
		activeTrackColor = thumbColor,
		activeTickColor = activeTickColor,
		inactiveTrackColor = inactiveTrackColor
	)

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(horizontal = 15.dp),
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		WavySlider(
			modifier = Modifier.fillMaxWidth(),
			enabled = playerState.currentTrack != null,
			colors = colors,
			waveHeight = waveHeight,
			animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
				waveAppearanceAnimationSpec = snap()
			),
			value = shownProgress,
			onValueChange = {
				isDragging = true
				dragProgress = it
			},
			onValueChangeFinished = {
				isDragging = false
				player.seek(dragProgress)
			},
			thumb = {
				SliderDefaults.Thumb(
					interactionSource = interactionSource,
					colors = colors,
					enabled = playerState.currentTrack != null,
					thumbSize = DpSize(6.dp, 24.dp),
					modifier = Modifier.clip(ContinuousCapsule)
				)
			}
		)
		Row(
			Modifier.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			CompositionLocalProvider(
				LocalTextStyle provides MaterialTheme.typography.bodyMedium,
				LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
			) {
				val duration = playerState.currentTrack?.duration
				if (duration != null) {
					if (expanded) {
						Text(((duration * shownProgress).toDouble().seconds).toHHMMSS())
						Text(duration.seconds.toHHMMSS())
					}
				} else {
					Text("--:--")
					Text("--:--")
				}
			}
		}
	}
}
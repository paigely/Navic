package paige.navic.ui.component.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
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
import paige.navic.shared.Ctx
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.shared.PlayerUiState
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.DropdownItem
import paige.navic.ui.screen.LyricsScreen
import paige.navic.util.toHHMMSS
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.Playlist
import kotlin.time.Duration.Companion.seconds

object MediaBarDefaults {
	val height = 117.9.dp
	val heightNoSeekbar = 75.dp
}

private class MediaBarScope(
	val player: MediaPlayerViewModel,
	val playerState: PlayerUiState,
	val ctx: Ctx,
	val animatedVisibilityScope: AnimatedVisibilityScope,
	val sharedTransitionScope: SharedTransitionScope,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaBar(expanded: Boolean) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsStateWithLifecycle()
	SharedTransitionLayout(Modifier.fillMaxHeight()) {
		AnimatedContent(
			expanded
		) { targetState ->
			MediaBarScope(
				player,
				playerState,
				ctx,
				this@AnimatedContent,
				this@SharedTransitionLayout
			).apply {
				if (!targetState) {
					MainContent()
				} else {
					DetailsContent()
				}
			}
		}
	}
}

@Composable
private fun MediaBarScope.MainContent() {
	var alwaysShowSeekbar by rememberBooleanSetting("alwaysShowSeekbar", true)
	Column(Modifier.height(if (alwaysShowSeekbar)
		MediaBarDefaults.height
	else MediaBarDefaults.heightNoSeekbar)) {
		Row(
			modifier = Modifier.padding(
				top = 15.dp
			).padding(horizontal = 15.dp),
			horizontalArrangement = Arrangement.spacedBy(10.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			AlbumArtContainer(
				Modifier.size(55.dp),
				expanded = false
			)
			Info(rowScope = this@Row)
			Controls(expanded = false)
		}
		if (alwaysShowSeekbar) {
			ProgressBar(expanded = false)
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.DetailsContent() {
	val pagerState = rememberPagerState(pageCount = { 2 })
	val currentIndex = playerState.currentIndex
	val currentTrack = playerState.tracks?.tracks?.getOrNull(currentIndex)
	Box(Modifier.fillMaxSize()) {
		AlbumArt(
			modifier = Modifier
				.fillMaxSize()
				.blur(150.dp, edgeTreatment = BlurredEdgeTreatment.Rectangle)
				.graphicsLayer { alpha = 0.75f }
				.drawWithContent {
					drawContent()
					drawRect(
						brush = Brush.verticalGradient(
							0f to Color.Black,
							.6f to Color.Black,
							.7f to Color.Transparent,
							startY = 0f,
							endY = size.height
						),
						blendMode = BlendMode.DstIn
					)
				}
		)
		HorizontalPager(
			state = pagerState,
			modifier = Modifier.fillMaxSize()
		) { page ->
			when (page) {
				0 -> PlayerView()
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
					else MaterialTheme.colorScheme.onSurface.copy(alpha = .25f),
					animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
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

//region views
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.PlayerView() {
	val paused = playerState.isPaused
	val artSize by animateDpAsState(
		if (paused)
			256.dp
		else 290.dp,
		animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
	)
	var moreShown by remember { mutableStateOf(false) }
	Column(
		Modifier.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
	) {
		AlbumArtContainer(
			modifier = Modifier.widthIn(0.dp, artSize).aspectRatio(1f),
			expanded = true
		)
		Column(Modifier.padding(15.dp)) {
			Row(
				Modifier.padding(15.dp, 0.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				Info(rowScope = this@Row)
				Box {
					IconButton(onClick = {
						moreShown = true
					}) {
						Icon(
							vectorResource(Res.drawable.more_vert),
							contentDescription = stringResource(Res.string.action_more)
						)
					}
					Dropdown(
						expanded = moreShown,
						onDismissRequest = {
							moreShown = false
						}
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
										is Album -> player.play(
											it.copy(
												song = it.song?.shuffled()
											), 0
										)
										is Playlist -> player.play(
											it.copy(
												entry = it.entry?.shuffled()
											), 0
										)
									}
								}
							}
						)
					}
				}
			}
			ProgressBar(expanded = true)
		}
		Controls(expanded = true)
	}
}
//endregion views

//region components
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.AlbumArtContainer(
	modifier: Modifier = Modifier,
	expanded: Boolean
) {
	val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()
	with(sharedTransitionScope) {
		Surface(
			modifier = modifier.sharedElement(
				sharedContentState = rememberSharedContentState(key = "image"),
				animatedVisibilityScope = animatedVisibilityScope,
				boundsTransform = { _, _ -> animationSpec }
			),
			shape = ContinuousRoundedRectangle(if (expanded) 18.dp else 12.dp),
			color = MaterialTheme.colorScheme.surfaceVariant
		) {
			AlbumArt()
		}
	}
}

@Composable
private fun MediaBarScope.AlbumArt(
	modifier: Modifier = Modifier
) {
	AsyncImage(
		modifier = modifier,
		model = playerState.tracks?.coverArt,
		contentDescription = playerState.tracks?.title,
		contentScale = ContentScale.Crop,
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.Info(
	rowScope: RowScope
) {
	val currentIndex = playerState.currentIndex
	val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()
	with(sharedTransitionScope) {
		with(rowScope) {
			Column(
				modifier = Modifier
					.sharedElement(
						sharedContentState = rememberSharedContentState(key = "info"),
						animatedVisibilityScope = animatedVisibilityScope,
						boundsTransform = { _, _ -> animationSpec }
					)
					.weight(1f),
				verticalArrangement = Arrangement.Center
			) {
				Text(
					playerState.tracks?.tracks?.getOrNull(currentIndex)?.title.orEmpty(),
					fontWeight = FontWeight(600),
					maxLines = 1
				)
				Text(
					playerState.tracks?.tracks?.getOrNull(currentIndex)?.artist.orEmpty(),
					style = MaterialTheme.typography.titleSmall,
					maxLines = 1
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.Controls(expanded: Boolean) {
	val paused = playerState.isPaused
	val size by animateDpAsState(
		if (expanded) 40.dp else 32.dp,
		animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
	)
	val contentPadding = if (!expanded) PaddingValues(horizontal = 4.dp) else ButtonDefaults.contentPaddingFor(
		60.dp
	)
	val shapes = ToggleButtonShapes(
		shape = ContinuousRoundedRectangle(16.dp),
		pressedShape = ContinuousRoundedRectangle(12.dp),
		checkedShape = ContinuousRoundedRectangle(12.dp)
	)

	val modifier = Modifier.size(size)
	val enabled = playerState.tracks != null
	val colors = ToggleButtonColors(
		containerColor = if (expanded)
			MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp)
		else Color.Transparent,
		contentColor = MaterialTheme.colorScheme.onSurface,
		disabledContainerColor = Color.Transparent,
		disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
		checkedContainerColor = MaterialTheme.colorScheme.primary,
		checkedContentColor = MaterialTheme.colorScheme.onPrimary
	)

	val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()

	with(sharedTransitionScope) {
		Row(
			modifier = Modifier.sharedElement(
				sharedContentState = rememberSharedContentState(key = "controls"),
				animatedVisibilityScope = animatedVisibilityScope,
				boundsTransform = { _, _ -> animationSpec }
			).then(
				if (expanded) {
					Modifier
						.clip(ContinuousCapsule)
						.background(MaterialTheme.colorScheme.surfaceContainer)
						.padding(8.dp)
						.clip(ContinuousCapsule)
				} else {
					Modifier
				}
			),
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
					content = {
						Icon(
							vectorResource(
								Res.drawable.skip_previous
							), null, modifier
						)
					}
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
					if (paused) {
						player.resume()
					} else {
						player.pause()
					}
				},
				content = {
					Icon(
						vectorResource(
							if (paused) Res.drawable.play_arrow else Res.drawable.pause
						), null, modifier
					)
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
				content = {
					Icon(
						vectorResource(
							Res.drawable.skip_next
						), null, modifier
					)
				}
			)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaBarScope.ProgressBar(expanded: Boolean) {
	val interactionSource = remember { MutableInteractionSource() }
	val progress = playerState.progress
	val paused = playerState.isPaused
	val waveHeight by animateDpAsState(
		if (paused)
			0.dp
		else SliderDefaults.WaveHeight,
		animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
	)
	val thumbColor by animateColorAsState(
		if (expanded)
			MaterialTheme.colorScheme.onSurface
		else Color.Unspecified,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)
	val inactiveTrackColor by animateColorAsState(
		if (expanded)
			MaterialTheme.colorScheme.onSurface.copy(alpha = .25f)
		else Color.Unspecified,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)
	val activeTickColor by animateColorAsState(
		if (expanded)
			MaterialTheme.colorScheme.onSurface
		else Color.Unspecified,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)
	val colors = SliderDefaults.colors(
		thumbColor = thumbColor,
		activeTrackColor = thumbColor,
		activeTickColor = activeTickColor,
		inactiveTrackColor = inactiveTrackColor
	)
	val animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Rect>()
	with(sharedTransitionScope) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 15.dp),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			WavySlider(
				modifier = Modifier
					.fillMaxWidth()
					.sharedElement(
						sharedContentState = rememberSharedContentState(key = "progress"),
						animatedVisibilityScope = animatedVisibilityScope,
						boundsTransform = { _, _ -> animationSpec }
					),
				colors = colors,
				enabled = playerState.tracks != null,
				waveHeight = waveHeight,
				animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
					waveAppearanceAnimationSpec = snap()
				),
				value = progress,
				onValueChange = { player.seek(it) },
				thumb = {
					SliderDefaults.Thumb(
						interactionSource = interactionSource,
						colors = colors,
						enabled = playerState.tracks != null,
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
							Text(((duration * progress).toDouble().seconds).toHHMMSS())
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
}
//endregion components

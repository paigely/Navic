package paige.navic.ui.components.layouts

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_not_playing
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.Settings
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.filled.Note
import paige.navic.icons.filled.Pause
import paige.navic.icons.filled.Play
import paige.navic.icons.filled.SkipNext
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.common.playPauseIconPainter

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerBar(
	modifier: Modifier = Modifier
) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val backStack = LocalNavStack.current

	val playerState by player.uiState.collectAsState()
	val track = playerState.currentTrack
	val coverUri = remember(track?.coverArt) {
		SessionManager.api.getCoverArtUrl(
			track?.coverArt,
			auth = true
		)
	}

	val detached = Settings.shared.detachedBar

	val spec = MaterialTheme.motionScheme.defaultSpatialSpec<Dp>()
	val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Dp>()

	val outerPadding by animateDpAsState(
		if (detached) 12.dp else 0.dp, effectsSpec
	)
	val contentPadding by animateDpAsState(
		if (detached) 10.dp else 16.dp, effectsSpec
	)
	val shadowRadius by animateDpAsState(
		if (detached) 5.dp else 8.dp, effectsSpec
	)
	val coverSize by animateDpAsState(
		if (detached) 48.dp else 55.dp, spec
	)
	val iconSize by animateDpAsState(
		if (detached) 24.dp else 32.dp, spec
	)
	val iconSpacing by animateDpAsState(
		if (detached) 8.dp else 12.dp, effectsSpec
	)
	val topCornerSize by animateDpAsState(
		if (detached) 20.dp else 18.dp, spec
	)
	val bottomCornerSize by animateDpAsState(
		if (detached) 20.dp else 0.dp, effectsSpec
	)

	val shape = ContinuousRoundedRectangle(
		topCornerSize, topCornerSize,
		bottomCornerSize, bottomCornerSize
	)

	val onClick = {
		if (!backStack.contains(Screen.Player)) {
			backStack.add(Screen.Player)
		}
	}

	Swiper(
		onSwipeLeft = {
			player.next()
		},
		onSwipeRight = {
			player.previous()
		}
	) {
		Box(
			modifier = Modifier
				.padding(bottom = outerPadding, start = outerPadding, end = outerPadding)
				.clip(shape)
		) {
			ListItem(
				modifier = modifier
					.dropShadow(
						shape,
						Shadow(
							radius = shadowRadius,
							alpha = 0.5f
						)
					)
					.pointerInput(Unit) {
						var totalDrag = 0f
						detectVerticalDragGestures(
							onVerticalDrag = { _, dragAmount ->
								totalDrag += dragAmount
							},
							onDragEnd = {
								if (totalDrag < -150f) {
									onClick()
								}
								totalDrag = 0f
							}
						)
					},
				contentPadding = PaddingValues(contentPadding),
				verticalAlignment = Alignment.CenterVertically,
				colors = ListItemDefaults.colors(
					containerColor = MaterialTheme.colorScheme.surfaceContainer
				),
				shapes = ListItemDefaults.shapes(
					shape = shape,
					selectedShape = shape,
					pressedShape = shape,
					focusedShape = shape,
					hoveredShape = shape,
					draggedShape = shape
				),
				onClick = onClick,
				onLongClick = onClick,
				leadingContent = {
					Box(contentAlignment = Alignment.Center) {
						AsyncImage(
							model = coverUri,
							contentDescription = null,
							contentScale = ContentScale.Crop,
							modifier = Modifier
								.clip(
									ContinuousRoundedRectangle(
										if (detached) 12.dp else 10.dp
									)
								)
								.background(MaterialTheme.colorScheme.surfaceVariant)
								.size(coverSize)
						)
						if (coverUri.isNullOrEmpty()) {
							Icon(
								imageVector = Icons.Filled.Note,
								contentDescription = null,
								tint = MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
							)
						}
					}
				},
				trailingContent = {
					Row(
						horizontalArrangement = Arrangement.spacedBy(iconSpacing)
					) {
						IconButton(
							onClick = {
								ctx.clickSound()
								if (playerState.isPaused) {
									player.resume()
								} else {
									player.pause()
								}
							},
							enabled = playerState.currentTrack != null
						) {
							val painter = playPauseIconPainter(playerState.isPaused)
							if (painter != null) {
								Icon(
									painter = painter,
									contentDescription = null,
									modifier = Modifier.size(iconSize)
								)
							} else {
								Icon(
									imageVector = if (playerState.isPaused)
										Icons.Filled.Play
									else Icons.Filled.Pause,
									contentDescription = null,
									modifier = Modifier.size(iconSize)
								)
							}
						}
						IconButton(
							onClick = {
								ctx.clickSound()
								player.next()
							},
							enabled = playerState.currentTrack != null
						) {
							Icon(
								imageVector = Icons.Filled.SkipNext,
								contentDescription = null,
								modifier = Modifier.size(iconSize)
							)
						}
					}
				},
				content = {
					track?.title?.let { title ->
						MarqueeText(title)
					}
				},
				supportingContent = {
					if (track != null) {
						track.artist?.let { artist ->
							MarqueeText(artist)
						}
					} else {
						MarqueeText(stringResource(Res.string.info_not_playing))
					}
				}
			)
			if (Settings.shared.showProgressInBar) {
				var dragging by remember { mutableStateOf(false) }
				val alpha by animateFloatAsState(
					if (dragging) 1f else .7f
				)
				val progress by animateFloatAsState(
					playerState.progress.coerceIn(0f, 1f)
				)
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.height(10.dp)
						.pointerInput(Unit) {
							if (track != null) {
								detectDragGestures(
									onDragStart = { dragging = true },
									onDragEnd = { dragging = false }
								) { change, _ ->
									player.seek(
										(change.position.x / size.width.toFloat()).coerceIn(
											0f,
											1f
										)
									)
									change.consume()
								}
							}
						}
						.align(Alignment.BottomStart),
					contentAlignment = Alignment.BottomStart
				) {
					Box(
						Modifier
							.background(MaterialTheme.colorScheme.surfaceContainerHighest)
							.fillMaxWidth()
							.height(3.dp)
					)
					Box(
						Modifier
							.background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
							.fillMaxWidth(if (track != null) progress else 0f)
							.height(3.dp)
					)
				}
			}
		}
	}
}

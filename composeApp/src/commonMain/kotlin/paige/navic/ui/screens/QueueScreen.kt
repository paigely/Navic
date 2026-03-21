package paige.navic.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Song
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_from_queue
import navic.composeapp.generated.resources.action_reorder
import navic.composeapp.generated.resources.info_no_queue
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.DragHandle
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.viewmodels.QueueViewModel
import paige.navic.utils.DraggableListState
import paige.navic.utils.dragHandle
import paige.navic.utils.draggableItems
import paige.navic.utils.fadeFromTop
import paige.navic.utils.rememberDraggableListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueScreen(
	viewModel: QueueViewModel = viewModel { QueueViewModel() }
) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val currentTrack = playerState.currentTrack
	val queue = playerState.queue

	val haptic = LocalHapticFeedback.current
	val draggableState = rememberDraggableListState(viewModel.listState) { from, to ->
		player.moveQueueItem(from, to)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	LazyColumn(
		modifier = Modifier.fillMaxSize().fadeFromTop(),
		state = draggableState.listState,
		contentPadding = WindowInsets.statusBars.asPaddingValues()
			+ WindowInsets.systemBars.asPaddingValues()
			+ PaddingValues(vertical = 70.dp, horizontal = 16.dp),
		verticalArrangement = if (queue.isNotEmpty())
			Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
		else Arrangement.Center
	) {
		draggableItems(
			state = draggableState,
			items = queue,
			key = { track -> track.id }
		) { track, isDragging ->
			val index = queue.indexOf(track)

			QueueScreenItem(
				index = index,
				count = queue.count(),
				track = track,
				isPlaying = currentTrack?.id == track.id
					&& !playerState.isPaused,
				isSelected = currentTrack?.id == track.id,
				isDragging = isDragging,
				draggableState = draggableState,
				onClick = {
					ctx.clickSound()
					if (currentTrack?.id != track.id) {
						player.playAt(index)
					}
				},
				onRemove = {
					haptic.performHapticFeedback(HapticFeedbackType.LongPress)
					player.removeFromQueue(index)
				}
			)
		}
		if (queue.isEmpty()) {
			item {
				ContentUnavailable(
					icon = Icons.Outlined.PlaylistRemove,
					label = stringResource(Res.string.info_no_queue)
				)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun QueueScreenItem(
	index: Int,
	count: Int,
	track: Song,
	isPlaying: Boolean,
	isSelected: Boolean,
	isDragging: Boolean,
	draggableState: DraggableListState,
	onClick: () -> Unit,
	onRemove: () -> Unit
) {
	val elevation by animateDpAsState(
		targetValue = if (isDragging) 8.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)

	val dismissState = rememberSwipeToDismissBoxState()

	LaunchedEffect(dismissState.currentValue) {
		if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
			onRemove()
			dismissState.snapTo(SwipeToDismissBoxValue.Settled)
		}
	}

	val color = MaterialTheme.colorScheme.surface.copy(
		alpha = if (isSelected) .7f else .5f
	)
	val contentColor = if (isSelected)
		MaterialTheme.colorScheme.primary
	else MaterialTheme.colorScheme.onSurface

	val supportingContentColor = if (isSelected)
		MaterialTheme.colorScheme.primary.copy(alpha = .7f)
	else MaterialTheme.colorScheme.onSurfaceVariant

	val itemShape = ListItemDefaults.segmentedShapes(index = index, count = count)

	SwipeToDismissBox(
		state = dismissState,
		enableDismissFromEndToStart = false,
		enableDismissFromStartToEnd = true,
		backgroundContent = {
			val backgroundColor by animateColorAsState(
				targetValue = when (dismissState.targetValue) {
					SwipeToDismissBoxValue.StartToEnd -> Color.Red
					else -> Color.Transparent
				}
			)
			val iconColor by animateColorAsState(
				targetValue = when (dismissState.targetValue) {
					SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onErrorContainer
					else -> MaterialTheme.colorScheme.onSurfaceVariant
				}
			)

			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(itemShape.shape)
					.background(color = backgroundColor)
					.padding(horizontal = 20.dp),
				contentAlignment = Alignment.CenterStart
			) {
				Icon(
					imageVector = Icons.Outlined.Delete,
					contentDescription = stringResource(Res.string.action_remove_from_queue),
					tint = iconColor
				)
			}
		},
		content = {
			Surface(
				shadowElevation = elevation,
				shape = itemShape.shape
			) {
				SegmentedListItem(
					onClick = onClick,
					colors = ListItemDefaults.colors(
						containerColor = color,
						selectedContainerColor = color,
						disabledContainerColor = color,
						draggedContainerColor = color,
						contentColor = contentColor,
						supportingContentColor = supportingContentColor
					),
					shapes = itemShape,
					verticalAlignment = Alignment.CenterVertically,
					content = { MarqueeText(track.title) },
					supportingContent = { MarqueeText(track.artistName) },
					leadingContent = {
						Text(
							text = "${index + 1}",
							modifier = Modifier.width(25.dp),
							style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
							fontWeight = FontWeight(400),
							fontSize = 13.sp,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							maxLines = 1,
							textAlign = TextAlign.Center
						)
					},
					trailingContent = {
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							if (isSelected) {
								Waveform(isPlaying)
							}
							IconButton(
								modifier = Modifier.dragHandle(
									state = draggableState,
									key = track.id
								),
								onClick = {}
							) {
								Icon(
									Icons.Outlined.DragHandle,
									contentDescription = stringResource(Res.string.action_reorder)
								)
							}
						}
					}
				)
			}
		}
	)
}

@Composable
private fun Waveform(
	isPlaying: Boolean
) {
	val transition = rememberInfiniteTransition()
	Row(
		modifier = Modifier.height(18.dp),
		horizontalArrangement = Arrangement.spacedBy(2.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		repeat(5) { index ->
			val fraction by transition.animateFloat(
				initialValue = 0.2f,
				targetValue = 1f,
				animationSpec = infiniteRepeatable(
					animation = tween(
						durationMillis = 400 + (index * 150),
						easing = FastOutSlowInEasing
					),
					repeatMode = RepeatMode.Reverse
				)
			)
			Box(
				modifier = Modifier
					.width(3.dp)
					.fillMaxHeight(if (isPlaying) fraction else .2f)
					.background(MaterialTheme.colorScheme.onSurface, shape = CircleShape)
			)
		}
	}
}
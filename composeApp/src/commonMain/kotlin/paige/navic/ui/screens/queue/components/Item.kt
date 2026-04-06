package paige.navic.ui.screens.queue.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_from_queue
import navic.composeapp.generated.resources.action_reorder
import navic.composeapp.generated.resources.info_not_available_offline
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.DragHandle
import paige.navic.icons.outlined.Offline
import paige.navic.ui.components.common.MarqueeText
import paige.navic.utils.DraggableListState
import paige.navic.utils.dragHandle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueScreenItem(
	index: Int,
	count: Int,
	track: DomainSong,
	isPlaying: Boolean,
	isSelected: Boolean,
	isDragging: Boolean,
	draggableState: DraggableListState,
	onClick: () -> Unit,
	onRemove: () -> Unit,
	isOffline: Boolean = false,
	isDownloaded: Boolean = false
) {
	val canPlay = !isOffline || isDownloaded

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
					enabled = canPlay,
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
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							maxLines = 1,
							textAlign = TextAlign.Center,
							autoSize = TextAutoSize.StepBased(6.sp, 13.sp)
						)
					},
					trailingContent = {
						Row(
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							if (!canPlay) {
								Icon(
									Icons.Outlined.Offline,
									stringResource(Res.string.info_not_available_offline),
									modifier = Modifier.size(20.dp)
								)
							}
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

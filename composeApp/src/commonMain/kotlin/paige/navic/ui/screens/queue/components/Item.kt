package paige.navic.ui.screens.queue.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_from_queue
import navic.composeapp.generated.resources.action_reorder
import navic.composeapp.generated.resources.info_not_available_offline
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.DragHandle
import paige.navic.icons.outlined.Offline
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.common.Waveform
import paige.navic.utils.DraggableListState
import paige.navic.utils.dragHandle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueScreenItem(
	index: Int,
	count: Int,
	song: DomainSong,
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
	val scope = rememberCoroutineScope()

	val color = if (isSelected)
		MaterialTheme.colorScheme.surfaceContainerHighest
	else MaterialTheme.colorScheme.surfaceContainerHigh

	val contentColor = if (isSelected)
		MaterialTheme.colorScheme.primary
	else MaterialTheme.colorScheme.onSurface

	val supportingContentColor = if (isSelected)
		MaterialTheme.colorScheme.primary.copy(alpha = .7f)
	else MaterialTheme.colorScheme.onSurfaceVariant

	val itemShape = ListItemDefaults.segmentedShapes(index = index, count = count)

	SwipeToDismissBox(
		state = dismissState,
		onDismiss = {
			onRemove()
			scope.launch {
				dismissState.reset()
			}
		},
		backgroundContent = {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(itemShape.shape)
					.background(MaterialTheme.colorScheme.errorContainer)
					.padding(horizontal = 20.dp)
			) {
				Icon(
					imageVector = Icons.Outlined.Delete,
					contentDescription = stringResource(Res.string.action_remove_from_queue),
					tint = MaterialTheme.colorScheme.onErrorContainer,
					modifier = Modifier.align(when (dismissState.dismissDirection) {
						SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
						else -> Alignment.CenterEnd
					})
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
					content = { MarqueeText(song.title) },
					supportingContent = { MarqueeText(song.artistName) },
					leadingContent = {
						CoverArt(
							modifier = Modifier.size(48.dp),
							coverArtId = song.coverArtId,
							shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
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
								Waveform(isPlaying = isPlaying)
							}
							IconButton(
								modifier = Modifier.dragHandle(
									state = draggableState,
									index = index
								),
								onClick = {}
							) {
								Icon(
									Icons.Outlined.DragHandle,
									contentDescription = stringResource(Res.string.action_reorder)
								)
							}
						}
					},
					contentPadding = PaddingValues(10.dp)
				)
			}
		}
	)
}

package paige.navic.ui.screens.track.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_queue
import navic.composeapp.generated.resources.info_not_available_offline
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Offline
import paige.navic.icons.outlined.Queue
import paige.navic.ui.components.common.MarqueeText
import paige.navic.utils.toHoursMinutesSeconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TracksScreenTrackRow(
	track: DomainSong,
	index: Int,
	count: Int,
	onClick: (() -> Unit),
	onLongClick: (() -> Unit),
	onAddToQueue: (() -> Unit),
	download: DownloadEntity? = null,
	isOffline: Boolean = false
) {
	val isDownloaded = download?.status == DownloadStatus.DOWNLOADED
	val canPlay = !isOffline || isDownloaded

	val itemShape = segmentedShapes(
		index = index,
		count = count
	)

	val dismissState = rememberSwipeToDismissBoxState()

	LaunchedEffect(dismissState.currentValue) {
		if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
			onAddToQueue()
			dismissState.snapTo(SwipeToDismissBoxValue.Settled)
		}
	}

	SwipeToDismissBox(
		modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.5.dp),
		state = dismissState,
		enableDismissFromStartToEnd = false,
		backgroundContent = {
			val backgroundColor by animateColorAsState(
				targetValue = when (dismissState.targetValue) {
					SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
					else -> Color.Transparent
				}
			)
			val iconColor by animateColorAsState(
				targetValue = when (dismissState.targetValue) {
					SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onPrimaryContainer
					else -> MaterialTheme.colorScheme.onSurfaceVariant
				}
			)

			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(itemShape.shape)
					.background(color = backgroundColor)
					.padding(horizontal = 20.dp),
				contentAlignment = Alignment.CenterEnd
			) {
				Icon(
					imageVector = Icons.Outlined.Queue,
					contentDescription = stringResource(Res.string.action_add_to_queue),
					tint = iconColor
				)
			}
		}
	) {
		SegmentedListItem(
			contentPadding = PaddingValues(14.dp),
			onClick = onClick,
			onLongClick = onLongClick,
			enabled = canPlay,
			shapes = itemShape,
			colors = ListItemDefaults.segmentedColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer,
				disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
			),
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
			content = {
				Column {
					MarqueeText(track.title)
					Text(
						track.artistName,
						style = MaterialTheme.typography.bodySmall,
						maxLines = 1
					)
				}
			},
			trailingContent = {
				Row(verticalAlignment = Alignment.CenterVertically) {
					if (!canPlay) {
						Icon(
							Icons.Outlined.Offline,
							stringResource(Res.string.info_not_available_offline),
							modifier = Modifier.size(20.dp)
						)
						Spacer(Modifier.width(6.dp))
					}
					if (download != null) {
						when (download.status) {
							DownloadStatus.DOWNLOADING -> {
								CircularProgressIndicator(
									progress = { download.progress },
									modifier = Modifier.size(16.dp),
									strokeWidth = 2.dp
								)
								Spacer(Modifier.width(8.dp))
							}

							DownloadStatus.DOWNLOADED -> {
								Icon(
									Icons.Outlined.Check,
									contentDescription = null,
									modifier = Modifier.size(16.dp),
									tint = MaterialTheme.colorScheme.primary
								)
								Spacer(Modifier.width(8.dp))
							}

							else -> {}
						}
					}
					track.duration.toHoursMinutesSeconds().let {
						Text(
							text = it,
							style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
							fontWeight = FontWeight(400),
							fontSize = 13.sp,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							maxLines = 1
						)
					}
				}
			}
		)
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun segmentedShapes(
	index: Int,
	count: Int,
	defaultShapes: ListItemShapes = ListItemDefaults.shapes(),
): ListItemShapes {
	val overrideShape = ContinuousRoundedRectangle(18.dp)
	return remember(index, count, defaultShapes, overrideShape) {
		when {
			count == 1 -> defaultShapes

			index == 0 -> {
				val defaultBaseShape = defaultShapes.shape
				if (defaultBaseShape is CornerBasedShape) {
					defaultShapes.copy(
						shape =
							defaultBaseShape.copy(
								topStart = overrideShape.topStart,
								topEnd = overrideShape.topEnd,
							)
					)
				} else {
					defaultShapes
				}
			}

			index == count - 1 -> {
				val defaultBaseShape = defaultShapes.shape
				if (defaultBaseShape is CornerBasedShape) {
					defaultShapes.copy(
						shape =
							defaultBaseShape.copy(
								bottomStart = overrideShape.bottomStart,
								bottomEnd = overrideShape.bottomEnd,
							)
					)
				} else {
					defaultShapes
				}
			}

			else -> defaultShapes
		}
	}
}

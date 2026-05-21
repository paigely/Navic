package paige.navic.ui.screens.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_clear_queue
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.info_no_queue
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.screens.queue.components.QueueScreenItem
import paige.navic.ui.screens.queue.viewmodels.QueueViewModel
import paige.navic.ui.theme.defaultFont
import paige.navic.utils.draggableItemsIndexed
import paige.navic.utils.rememberDraggableListState
import kotlin.time.DurationUnit

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueScreen() {
	val viewModel = koinViewModel<QueueViewModel>()
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
	val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
	val queue = playerState.queue

	val haptic = LocalHapticFeedback.current
	val draggableState = rememberDraggableListState(viewModel.listState) { from, to ->
		player.moveQueueItem(from, to)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	LaunchedEffect(playerState.currentIndex) {
		runCatching {
			if (queue.isNotEmpty()) {
				draggableState.listState.scrollToItem(
					playerState.currentIndex.coerceAtLeast(0)
				)
			}
		}
	}

	val totalDurationText = remember(queue) {
		val totalSeconds = queue.sumOf { it.duration.toInt(DurationUnit.SECONDS) }

		val hours = totalSeconds / 3600
		val minutes = (totalSeconds % 3600) / 60
		val seconds = totalSeconds % 60

		buildString {
			if (hours > 0) {
				append("${hours}h ")
			}

			if (minutes > 0 || hours > 0) {
				append("${minutes}m ")
			}

			append("${seconds}s")
		}
	}

	val songsText = pluralStringResource(
		Res.plurals.count_songs,
		queue.size,
		queue.size
	)

	Column(
		modifier = Modifier
			.fillMaxSize()
			.clip(ContinuousRoundedRectangle(topStart = 16.dp, topEnd = 16.dp))
	) {
		if (queue.isNotEmpty()) {
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(
						top = 8.dp,
						bottom = 8.dp,
						start = 16.dp,
						end = 8.dp
					),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "$songsText • $totalDurationText",
					style = MaterialTheme.typography.titleMedium,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					fontFamily = defaultFont(round = 100f)
				)
				TextButton(
					onClick = {
						ctx.clickSound()
						haptic.performHapticFeedback(HapticFeedbackType.LongPress)
						player.clearQueue()
					}
				) {
					Text(stringResource(Res.string.action_clear_queue))
				}
			}
		}

		LazyColumn(
			modifier = Modifier
				.padding(horizontal = 12.dp)
				.fillMaxSize(),
			state = draggableState.listState,
			verticalArrangement = if (queue.isNotEmpty())
				Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
			else Arrangement.Center
		) {
			draggableItemsIndexed(
				state = draggableState,
				items = queue,
				key = { index, _ -> index }
			) { index, song, isDragging ->
				QueueScreenItem(
					index = index,
					count = queue.count(),
					song = song,
					isPlaying = playerState.currentIndex == index
						&& !playerState.isPaused,
					isSelected = playerState.currentIndex == index,
					isDragging = isDragging,
					draggableState = draggableState,
					onClick = {
						ctx.clickSound()
						if (playerState.currentIndex != index) {
							player.playAt(index)
							backStack.remove(Screen.Queue)
						}
					},
					onRemove = {
						haptic.performHapticFeedback(HapticFeedbackType.LongPress)
						player.removeFromQueue(index)
					},
					isOffline = !isOnline,
					isDownloaded = downloadedSongs.containsKey(song.id)
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
}

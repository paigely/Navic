package paige.navic.ui.screens.queue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_queue
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.screens.queue.components.QueueScreenItem
import paige.navic.ui.screens.queue.viewmodels.QueueViewModel
import paige.navic.utils.draggableItems
import paige.navic.utils.fadeFromTop
import paige.navic.utils.rememberDraggableListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun QueueScreen() {
	val viewModel = koinViewModel<QueueViewModel>()
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()
	val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
	val downloadedSongs by viewModel.downloadedSongs.collectAsStateWithLifecycle()
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
				},
				isOffline = !isOnline,
				isDownloaded = downloadedSongs.containsKey(track.id)
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

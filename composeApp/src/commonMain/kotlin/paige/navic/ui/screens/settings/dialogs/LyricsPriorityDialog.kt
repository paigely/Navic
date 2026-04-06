package paige.navic.ui.screens.settings.dialogs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.action_reorder
import navic.composeapp.generated.resources.option_lyrics_priority
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.domain.repositories.LyricsProvider
import paige.navic.icons.Icons
import paige.navic.icons.outlined.DragHandle
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.screens.settings.viewmodels.LyricsPriorityViewModel
import paige.navic.utils.DraggableListState
import paige.navic.utils.UiState
import paige.navic.utils.dragHandle
import paige.navic.utils.draggableItems
import paige.navic.utils.rememberDraggableListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LyricsPriorityDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val viewModel = koinViewModel<LyricsPriorityViewModel>()

	val ctx = LocalCtx.current
	val haptic = LocalHapticFeedback.current
	val state by viewModel.state.collectAsState()

	val draggableState = rememberDraggableListState { from, to ->
		viewModel.move(from, to)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	when (state) {
		is UiState.Loading -> return
		is UiState.Error -> ErrorBox(state as UiState.Error)
		is UiState.Success -> {
			val config = (state as UiState.Success).data
			AlertDialog(
				title = {
					Text(stringResource(Res.string.option_lyrics_priority))
				},
				text = {
					LazyColumn(
						modifier = Modifier
							.fillMaxWidth()
							.heightIn(max = 300.dp),
						state = draggableState.listState,
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						draggableItems(
							state = draggableState,
							items = config.priority,
							key = { provider -> provider.name }
						) { provider, isDragging ->
							ProviderRow(
								provider = provider,
								isDragging = isDragging,
								state = draggableState
							)
						}
					}
				},
				onDismissRequest = onDismissRequest,
				confirmButton = {
					Button(onClick = {
						ctx.clickSound()
						onDismissRequest()
					}) {
						Text(stringResource(Res.string.action_ok))
					}
				}
			)
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProviderRow(
	state: DraggableListState,
	provider: LyricsProvider,
	isDragging: Boolean
) {
	val elevation by animateDpAsState(
		if (isDragging) 4.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)
	Surface(
		shadowElevation = elevation,
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.large
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(provider.displayName)
			IconButton(
				modifier = Modifier.dragHandle(
					state = state,
					key = provider.name
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
}
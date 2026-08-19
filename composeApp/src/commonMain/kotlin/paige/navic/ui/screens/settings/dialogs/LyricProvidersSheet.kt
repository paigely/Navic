package paige.navic.ui.screens.settings.dialogs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_reorder
import navic.composeapp.generated.resources.info_lyric_provider_disclaimer
import navic.composeapp.generated.resources.title_lyric_providers
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.domain.models.lyrics.LyricsProvider
import paige.navic.icons.Icons
import paige.navic.icons.outlined.DragHandle
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.sheets.ModalBottomSheet
import paige.navic.ui.core.UiState
import paige.navic.ui.screens.settings.viewmodels.LyricsPriorityViewModel
import paige.navic.util.core.label
import paige.navic.util.ui.DraggableListState
import paige.navic.util.ui.dragHandle
import paige.navic.util.ui.draggableItems
import paige.navic.util.ui.rememberDraggableListState

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LyricsPrioritySheet(
	presented: Boolean,
	onDismissRequest: () -> Unit
) {
	if (!presented) return

	val viewModel = koinViewModel<LyricsPriorityViewModel>()

	val haptic = LocalHapticFeedback.current
	val state by viewModel.state.collectAsState()

	val draggableState = rememberDraggableListState { from, to ->
		viewModel.move(from, to)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = {
			Surface(
				modifier = Modifier.padding(vertical = 6.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				shape = ContinuousCapsule,
			) {
				Box(Modifier.size(width = 32.dp, height = 4.dp))
			}
		},
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					top = 16.dp,
					left = 12.dp,
					right = 12.dp,
					bottom = 8.dp
				)
			)
		}
	) {
		Text(
			text = stringResource(Res.string.title_lyric_providers),
			style = MaterialTheme.typography.titleLarge
		)
		Text(
			text = stringResource(Res.string.info_lyric_provider_disclaimer),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Spacer(Modifier.height(16.dp))

		when (state) {
			is UiState.Loading -> return@ModalBottomSheet
			is UiState.Error -> ErrorBox(state as UiState.Error)
			is UiState.Success -> {
				val config = (state as UiState.Success).data
				LazyColumn(
					modifier = Modifier.fillMaxWidth().wrapContentHeight(),
					state = draggableState.listState,
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					draggableItems(
						state = draggableState,
						items = config.providers,
						key = { provider -> provider.id }
					) { provider, isDragging ->
						ProviderRow(
							provider = provider,
							state = draggableState,
							isDragging = isDragging,
							onToggleEnabled = {
								viewModel.toggleEnabled(provider.id)
							}
						)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProviderRow(
	provider: LyricsProvider,
	state: DraggableListState,
	isDragging: Boolean,
	onToggleEnabled: () -> Unit
) {
	val elevation by animateDpAsState(
		if (isDragging) 4.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)
	Surface(
		shadowElevation = elevation,
		modifier = Modifier.fillMaxWidth(),
		shape = MaterialTheme.shapes.large,
		color = MaterialTheme.colorScheme.surfaceContainer
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 16.dp, vertical = 8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				checked = provider.enabled,
				onCheckedChange = { _ -> onToggleEnabled() }
			)
			Text(provider.id.label())
			IconButton(
				modifier = Modifier.dragHandle(
					state = state,
					key = provider.id
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

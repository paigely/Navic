package paige.navic.ui.components.dialogs

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.capsule.ContinuousRoundedRectangle
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.option_navbar_tab_positions
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.NavbarConfig
import paige.navic.data.models.NavbarTab
import paige.navic.icons.Icons
import paige.navic.icons.outlined.DragHandle
import paige.navic.ui.components.common.ErrorBox
import paige.navic.utils.UiState
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class NavtabsViewModel(
	private val settings: Settings,
	private val json: Json
) : ViewModel() {
	private val _state = MutableStateFlow<UiState<NavbarConfig>>(UiState.Loading)
	val state = _state.asStateFlow()

	init {
		try {
			_state.value = UiState.Success(loadConfig())
		} catch (e: Exception) {
			_state.value = UiState.Error(e)
		}
	}

	private fun loadConfig(): NavbarConfig {
		val raw = settings.getStringOrNull(NavbarConfig.KEY)
			?: return NavbarConfig.default
		val config: NavbarConfig = json.decodeFromString(raw)
		return config.takeIf { it.version == NavbarConfig.VERSION }
			?: NavbarConfig.default
	}

	private fun setConfig(newConfig: NavbarConfig) {
		_state.value = UiState.Success(newConfig)
		settings[NavbarConfig.KEY] = json.encodeToString(newConfig)
	}

	fun move(from: Int, to: Int) {
		val config = (_state.value as UiState.Success).data
		setConfig(config.copy(
			tabs = config.tabs.toMutableList().apply {
				add(to, removeAt(from))
			}
		))
	}

	fun toggleVisibility(id: NavbarTab.Id) {
		val config = (_state.value as UiState.Success).data
		setConfig(
			config.copy(
				tabs = config.tabs.map {
					if (it.id == id) it.copy(visible = !it.visible) else it
				}
			)
		)
	}
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavtabsDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit,
	viewModel: NavtabsViewModel = viewModel { NavtabsViewModel(Settings(), Json) }
) {
	if (!presented) return

	val haptic = LocalHapticFeedback.current
	val lazyListState = rememberLazyListState()
	val state by viewModel.state.collectAsState()

	val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
		viewModel.move(from.index, to.index)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

	when (state) {
		is UiState.Loading -> return
		is UiState.Error -> ErrorBox(state as UiState.Error)
		is UiState.Success -> {
			val config = (state as UiState.Success).data
			AlertDialog(
				title = {
					Text(stringResource(Res.string.option_navbar_tab_positions))
				},
				text = {
					LazyColumn(
						modifier = Modifier
							.fillMaxWidth()
							.heightIn(max = 300.dp),
						state = lazyListState,
						verticalArrangement = Arrangement.spacedBy(8.dp)
					) {
						items(
							items = config.tabs,
							key = { tab -> tab.id }
						) { tab ->
							ReorderableItem(
								reorderableState,
								key = tab.id,
								animateItemModifier = Modifier.animateItem(
									placementSpec = MaterialTheme.motionScheme.fastSpatialSpec()
								)
							) { isDragging ->
								NavtabRow(
									tab = tab,
									isDragging = isDragging,
									onToggleVisibility = {
										viewModel.toggleVisibility(tab.id)
									}
								)
							}
						}
					}
				},
				onDismissRequest = onDismissRequest,
				confirmButton = {
					Button(onClick = onDismissRequest) {
						Text(stringResource(Res.string.action_ok))
					}
				}
			)
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReorderableCollectionItemScope.NavtabRow(
	tab: NavbarTab,
	isDragging: Boolean,
	onToggleVisibility: () -> Unit
) {
	val haptic = LocalHapticFeedback.current
	val elevation by animateDpAsState(
		if (isDragging) 4.dp else 0.dp,
		animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()
	)

	Surface(
		shadowElevation = elevation,
		modifier = Modifier.fillMaxWidth(),
		shape = ContinuousRoundedRectangle(14.dp)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(8.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Checkbox(
				enabled = tab.id != NavbarTab.Id.LIBRARY,
				checked = tab.visible,
				onCheckedChange = {
					onToggleVisibility()
				}
			)
			Text(tab.id.name.lowercase().replaceFirstChar { it.uppercase() })
			IconButton(
				modifier = Modifier.draggableHandle(
					onDragStarted = {
						haptic.performHapticFeedback(
							HapticFeedbackType.GestureThresholdActivate
						)
					},
					onDragStopped = {
						haptic.performHapticFeedback(
							HapticFeedbackType.GestureEnd
						)
					}
				),
				onClick = {}
			) {
				Icon(
					Icons.Outlined.DragHandle,
					contentDescription = null
				)
			}
		}
	}
}

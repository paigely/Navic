package paige.navic.ui.component.dialog

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import kotlinx.serialization.json.Json
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.drag_handle
import navic.composeapp.generated.resources.option_navbar_tab_positions
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.data.model.NAVBAR_CONFIG_KEY
import paige.navic.data.model.NAVBAR_CONFIG_VERSION
import paige.navic.data.model.NavTabId
import paige.navic.data.model.NavbarConfig
import paige.navic.data.model.defaultNavbarConfig
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

class NavbarTabsViewModel(
	private val settings: Settings,
	private val json: Json
) : ViewModel() {

	var config by mutableStateOf(loadConfig())
		private set

	private fun loadConfig(): NavbarConfig {
		val raw = settings.getString(NAVBAR_CONFIG_KEY, "")
		val config = if (raw.isNotEmpty())
			json.decodeFromString(raw)
		else
			defaultNavbarConfig
		return if (config.version != NAVBAR_CONFIG_VERSION)
			defaultNavbarConfig
		else config
	}

	private fun saveConfig(newConfig: NavbarConfig) {
		config = newConfig
		settings[NAVBAR_CONFIG_KEY] = json.encodeToString(newConfig)
	}

	fun move(from: Int, to: Int) {
		val list = config.tabs.toMutableList()
		list.add(to, list.removeAt(from))
		saveConfig(config.copy(tabs = list))
	}

	fun toggleVisibility(id: NavTabId) {
		saveConfig(
			config.copy(
				tabs = config.tabs.map {
					if (it.id == id) it.copy(visible = !it.visible) else it
				}
			)
		)
	}
}


@Composable
fun NavbarTabsDialog(
	presented: Boolean,
	onDismissRequest: () -> Unit,
	viewModel: NavbarTabsViewModel = viewModel { NavbarTabsViewModel(Settings(), Json) }
) {
	if (!presented) return

	val haptic = LocalHapticFeedback.current
	val lazyListState = rememberLazyListState()
	val config = viewModel.config

	val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
		viewModel.move(from.index, to.index)
		haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
	}

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
					key = { it.id }
				) { tab ->
					ReorderableItem(
						reorderableState,
						key = tab.id
					) { isDragging ->
						val elevation by animateDpAsState(
							if (isDragging) 4.dp else 0.dp
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
									enabled = tab.id != NavTabId.LIBRARY,
									checked = tab.visible,
									onCheckedChange = {
										viewModel.toggleVisibility(tab.id)
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
										vectorResource(Res.drawable.drag_handle),
										contentDescription = null
									)
								}
							}
						}
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

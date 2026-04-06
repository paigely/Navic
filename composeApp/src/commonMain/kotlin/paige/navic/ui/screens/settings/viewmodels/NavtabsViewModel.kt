package paige.navic.ui.screens.settings.viewmodels

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import paige.navic.data.models.NavbarConfig
import paige.navic.data.models.NavbarTab
import paige.navic.utils.UiState

class NavtabsViewModel(
	private val settings: Settings,
	private val json: Json
) : ViewModel() {
	private val _state = MutableStateFlow<UiState<NavbarConfig>>(UiState.Loading())
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

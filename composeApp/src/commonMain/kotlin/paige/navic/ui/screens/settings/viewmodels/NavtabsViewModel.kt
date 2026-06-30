package paige.navic.ui.screens.settings.viewmodels

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import paige.navic.domain.models.settings.NavbarConfig
import paige.navic.domain.models.settings.NavbarTab
import paige.navic.ui.core.UiState

class NavtabsViewModel(
	private val settings: Settings
) : ViewModel() {
	private val json = Json

	val state: StateFlow<UiState<NavbarConfig>>
		field = MutableStateFlow<UiState<NavbarConfig>>(UiState.Loading())

	init {
		try {
			state.value = UiState.Success(loadConfig())
		} catch (e: Exception) {
			state.value = UiState.Error(e)
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
		state.value = UiState.Success(newConfig)
		settings[NavbarConfig.KEY] = json.encodeToString(newConfig)
	}

	fun move(from: Int, to: Int) {
		val config = (state.value as UiState.Success).data
		setConfig(
			config.copy(
				tabs = config.tabs.toMutableList().apply {
					add(to, removeAt(from))
				}
			))
	}

	fun toggleVisibility(id: NavbarTab.Id) {
		val config = (state.value as UiState.Success).data
		setConfig(
			config.copy(
				tabs = config.tabs.map {
					if (it.id == id) it.copy(visible = !it.visible) else it
				}
			)
		)
	}
}

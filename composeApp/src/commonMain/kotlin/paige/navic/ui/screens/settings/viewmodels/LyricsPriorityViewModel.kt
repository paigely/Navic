package paige.navic.ui.screens.settings.viewmodels

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import paige.navic.domain.repositories.LyricsConfig
import paige.navic.utils.UiState

class LyricsPriorityViewModel(
    private val settings: Settings = Settings(),
    private val json: Json = Json.Default
) : ViewModel() {
	private val _state = MutableStateFlow<UiState<LyricsConfig>>(UiState.Loading())
	val state = _state.asStateFlow()

	companion object {
		const val KEY = "lyrics_config_prefs"
	}

	init {
		loadConfig()
	}

	private fun loadConfig() {
		try {
			val raw = settings.getStringOrNull(KEY)
			val config: LyricsConfig = if (raw != null) {
				json.decodeFromString(raw)
			} else {
                LyricsConfig()
			}
			_state.value = UiState.Success(config)
		} catch (e: Exception) {
			_state.value = UiState.Error(e)
		}
	}

	private fun setConfig(newConfig: LyricsConfig) {
		_state.value = UiState.Success(newConfig)
		settings[KEY] = json.encodeToString(newConfig)
	}

	fun move(from: Int, to: Int) {
		val currentState = _state.value
		if (currentState is UiState.Success) {
			val config = currentState.data
			setConfig(
				config.copy(
					priority = config.priority.toMutableList().apply {
						add(to, removeAt(from))
					}
				)
			)
		}
	}
}
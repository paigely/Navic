package paige.navic.ui.screens.settings.viewmodels

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import paige.navic.domain.models.lyrics.LyricsConfig
import paige.navic.ui.core.UiState

class LyricsPriorityViewModel(
	private val settings: Settings
) : ViewModel() {
	private val json = Json

	val state: StateFlow<UiState<LyricsConfig>>
		field = MutableStateFlow<UiState<LyricsConfig>>(UiState.Loading())

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
			state.value = UiState.Success(config)
		} catch (e: Exception) {
			state.value = UiState.Error(e)
		}
	}

	private fun setConfig(newConfig: LyricsConfig) {
		state.value = UiState.Success(newConfig)
		settings[KEY] = json.encodeToString(newConfig)
	}

	fun move(from: Int, to: Int) {
		val currentState = state.value
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

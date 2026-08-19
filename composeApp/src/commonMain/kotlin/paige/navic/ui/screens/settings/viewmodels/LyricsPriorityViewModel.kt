package paige.navic.ui.screens.settings.viewmodels

import androidx.lifecycle.ViewModel
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import paige.navic.domain.models.lyrics.LyricsConfig
import paige.navic.domain.models.lyrics.LyricsProvider
import paige.navic.ui.core.UiState

class LyricsPriorityViewModel(
	private val settings: Settings
) : ViewModel() {
	private val json = Json

	val state: StateFlow<UiState<LyricsConfig>>
		field = MutableStateFlow<UiState<LyricsConfig>>(UiState.Loading())

	init {
		try {
			state.value = UiState.Success(loadConfig())
		} catch (e: Exception) {
			state.value = UiState.Error(e)
		}
	}

	private fun loadConfig(): LyricsConfig {
		val raw = settings.getStringOrNull(LyricsConfig.KEY)
			?: return LyricsConfig.Default
		val config: LyricsConfig = json.decodeFromString(raw)
		return config.takeIf { it.version == LyricsConfig.VERSION }
			?: LyricsConfig.Default
	}

	private fun setConfig(newConfig: LyricsConfig) {
		state.value = UiState.Success(newConfig)
		settings[LyricsConfig.KEY] = json.encodeToString(newConfig)
	}

	fun move(from: Int, to: Int) {
		val config = (state.value as UiState.Success).data
		setConfig(
			config.copy(
				providers = config.providers.toMutableList().apply {
					add(to, removeAt(from))
				}
			))
	}

	fun toggleEnabled(id: LyricsProvider.Id) {
		val config = (state.value as UiState.Success).data
		setConfig(
			config.copy(
				providers = config.providers.map {
					if (it.id == id) it.copy(enabled = !it.enabled) else it
				}
			)
		)
	}
}

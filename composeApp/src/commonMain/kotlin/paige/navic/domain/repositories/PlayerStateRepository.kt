package paige.navic.domain.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import paige.navic.ui.core.PlayerUiState
import paige.navic.util.core.Logger

class PlayerStateRepository(
	private val preferences: DataStore<Preferences>
) {
	private val json = Json
	private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	val state = preferenceStateFlow {
		return@preferenceStateFlow try {
			json.decodeFromString<PlayerUiState>(
				it[KEY_STATE] ?: return@preferenceStateFlow null
			)
		} catch(ex: SerializationException) {
			Logger.e("PlayerStateRepository", "failed to deserialise state", ex)
			null
		} catch(ex: Exception) {
			Logger.e("PlayerStateRepository", "failed to read state", ex)
			null
		}
	}

	suspend fun setState(value: PlayerUiState) {
		try {
			preferences.edit { it[KEY_STATE] = json.encodeToString(value) }
		} catch (ex: SerializationException) {
			Logger.e("PlayerStateRepository", "failed to serialise state", ex)
		} catch (ex: Exception) {
			Logger.e("PlayerStateRepository", "failed to save state", ex)
		}
	}

	private inline fun <T> preferenceStateFlow(crossinline transform: (Preferences) -> T): StateFlow<T> {
		return preferences.data
			.map(transform)
			.stateIn(
				scope = scope,
				started = SharingStarted.Eagerly,
				initialValue = transform(emptyPreferences())
			)
	}

	private companion object {
		val KEY_STATE = stringPreferencesKey("player_state")
	}
}

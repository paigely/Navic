package paige.navic.domain.repositories

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath
import paige.navic.shared.synchronized

class PlayerStateRepository(
	private val dataStore: DataStore<Preferences>
) {
	private val stateKey = stringPreferencesKey("player_ui_state_key")

	suspend fun saveState(stateJson: String) {
		dataStore.edit { prefs ->
			prefs[stateKey] = stateJson
		}
	}

	suspend fun loadState(): String? {
		return dataStore.data.map { prefs ->
			prefs[stateKey]
		}.firstOrNull()
	}

	companion object {
		const val DATASTORE_FILE_NAME = "playback_session.preferences_pb"

		private var instance: DataStore<Preferences>? = null

		fun getInstance(producePath: () -> String): DataStore<Preferences> {
			return instance ?: synchronized(this) {
				instance ?: PreferenceDataStoreFactory.createWithPath(
					produceFile = { producePath().toPath() }
				).also { instance = it }
			}
		}
	}
}

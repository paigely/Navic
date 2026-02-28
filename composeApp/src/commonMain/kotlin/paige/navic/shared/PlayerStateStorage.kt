package paige.navic.shared

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import okio.Path.Companion.toPath

const val DATASTORE_FILE_NAME = "playback_session.preferences_pb"

interface PlayerStateStorage {
	suspend fun saveState(stateJson: String)
	suspend fun loadState(): String?
}

object DataStoreSingleton {
	private var instance: DataStore<Preferences>? = null

	fun getInstance(producePath: () -> String): DataStore<Preferences> {
		return instance ?: synchronized(this) {
			instance ?: PreferenceDataStoreFactory.createWithPath(
				produceFile = { producePath().toPath() }
			).also { instance = it }
		}
	}
}

class DataStorePlayerStorage(
	private val dataStore: DataStore<Preferences>
) : PlayerStateStorage {

	private val stateKey = stringPreferencesKey("player_ui_state_key")

	override suspend fun saveState(stateJson: String) {
		dataStore.edit { prefs ->
			prefs[stateKey] = stateJson
		}
	}

	override suspend fun loadState(): String? {
		return dataStore.data.map { prefs ->
			prefs[stateKey]
		}.firstOrNull()
	}
}

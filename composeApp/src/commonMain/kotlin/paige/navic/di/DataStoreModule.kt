package paige.navic.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import okio.Path
import org.koin.core.module.Module

// eventually kmp settings should probably be replaced with this
fun createDataStore(producePath: () -> Path): DataStore<Preferences> {
	return PreferenceDataStoreFactory.createWithPath(produceFile = producePath)
}

const val DATA_STORE_FILE_NAME = "preferences.preferences_pb"

expect val dataStoreModule: Module

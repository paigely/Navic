package paige.navic.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import okio.Path.Companion.toOkioPath
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val dataStoreModule = module {
	single<DataStore<Preferences>> {
		createDataStore(
			producePath = {
				androidContext().filesDir.resolve(DATA_STORE_FILE_NAME).toOkioPath()
			}
		)
	}
}

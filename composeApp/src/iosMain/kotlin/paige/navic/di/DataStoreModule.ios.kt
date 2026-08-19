package paige.navic.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val dataStoreModule = module {
	single<DataStore<Preferences>> {
		createDataStore(
			producePath = {
				val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
					directory = NSDocumentDirectory,
					inDomain = NSUserDomainMask,
					appropriateForURL = null,
					create = false,
					error = null
				)
				"${requireNotNull(documentDirectory?.path)}/$DATA_STORE_FILE_NAME".toPath()
			}
		)
	}
}

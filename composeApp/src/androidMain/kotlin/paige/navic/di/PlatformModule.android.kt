package paige.navic.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import paige.navic.data.database.CacheDatabase
import paige.navic.shared.AndroidMediaPlayerViewModel
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.ShareManager
import paige.navic.managers.StorageManager
import paige.navic.shared.MediaPlayerViewModel

actual val platformModule = module {
	single<CacheDatabase> {
		val dbPath = androidApplication()
			.getDatabasePath("cache.db")
			.absolutePath
		Room
			.databaseBuilder<CacheDatabase>(get(), dbPath)
			.setDriver(BundledSQLiteDriver())
			.build()
	}

	single<PlayerStateRepository> {
		val context = androidApplication()
		val producePath = {
			context.filesDir.resolve(PlayerStateRepository.DATASTORE_FILE_NAME).absolutePath
		}
		PlayerStateRepository(PlayerStateRepository.getInstance(producePath))
	}

	viewModel<MediaPlayerViewModel> {
		AndroidMediaPlayerViewModel(
			application = androidApplication(),
			stateRepository = get(),
			trackRepository = get(),
			albumDao = get(),
			downloadManager = get(),
			connectivityManager = get()
		)
	}

	single<ShareManager> {
		ShareManager(context = get())
	}

	single<StorageManager> {
		StorageManager(context = androidApplication())
	}

	single<ConnectivityManager> {
		ConnectivityManager(
			context = androidApplication(),
			scope = get()
		)
	}
}

package paige.navic.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import paige.navic.data.database.CacheDatabase
import paige.navic.data.database.DownloadDatabase
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.ShareManager
import paige.navic.managers.StorageManager
import paige.navic.managers.SyncScheduler
import paige.navic.shared.AndroidMediaPlayerViewModel
import paige.navic.shared.MediaPlayerViewModel

actual val platformModule = module {
	single<CacheDatabase> {
		val dbPath = androidApplication()
			.getDatabasePath("cache.db")
			.absolutePath
		Room
			.databaseBuilder<CacheDatabase>(get(), dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single<DownloadDatabase> {
		val dbPath = androidApplication()
			.getDatabasePath("downloads.db")
			.absolutePath
		Room
			.databaseBuilder<DownloadDatabase>(get(), dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single {
		Json {
			ignoreUnknownKeys = true
			coerceInputValues = true
			encodeDefaults = true
		}
	}

	single<PlayerStateRepository> {
		PlayerStateRepository(
			playerStateDao = get(),
			json = get()
		)
	}

	viewModel<MediaPlayerViewModel> {
		AndroidMediaPlayerViewModel(
			application = androidApplication(),
			stateRepository = get(),
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

	singleOf(::SyncScheduler)
}

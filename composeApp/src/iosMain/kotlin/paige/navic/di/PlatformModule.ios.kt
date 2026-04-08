package paige.navic.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import coil3.PlatformContext
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import paige.navic.data.database.CacheDatabase
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.ShareManager
import paige.navic.managers.StorageManager
import paige.navic.shared.IOSMediaPlayerViewModel
import paige.navic.shared.MediaPlayerViewModel
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual val platformModule = module {
	single<CacheDatabase> {
		val dbPath = documentDirectory() + "/cache.db"
		Room
			.databaseBuilder<CacheDatabase>(dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single<PlayerStateRepository> {
		val producePath = {
			@OptIn(ExperimentalForeignApi::class)
			val directory = NSFileManager.defaultManager.URLForDirectory(
				directory = NSDocumentDirectory,
				inDomain = NSUserDomainMask,
				appropriateForURL = null,
				create = true,
				error = null
			)
			directory?.path + "/${PlayerStateRepository.DATASTORE_FILE_NAME}"
		}
		PlayerStateRepository(PlayerStateRepository.getInstance(producePath))
	}

	viewModel<MediaPlayerViewModel> {
		IOSMediaPlayerViewModel(
			stateRepository = get(),
			collectionRepository = get(),
			downloadManager = get(),
			connectivityManager = get()
		)
	}

	singleOf(::ShareManager)
	single<PlatformContext> { PlatformContext.INSTANCE }
	single<StorageManager> { StorageManager() }
	single<ConnectivityManager> { ConnectivityManager(get()) }
}

@OptIn(ExperimentalForeignApi::class)
private fun documentDirectory(): String {
	val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
		directory = NSDocumentDirectory,
		inDomain = NSUserDomainMask,
		appropriateForURL = null,
		create = false,
		error = null,
	)
	return requireNotNull(documentDirectory?.path)
}

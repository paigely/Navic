package paige.navic.di

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.data.database.CacheDatabase
import paige.navic.data.database.DownloadDatabase
import paige.navic.domain.manager.AppIconManager
import paige.navic.domain.manager.ConnectivityManager
import paige.navic.domain.manager.LinkManager
import paige.navic.domain.manager.LogManager
import paige.navic.domain.manager.PermissionManager
import paige.navic.domain.manager.ShareManager
import paige.navic.domain.manager.StorageManager
import paige.navic.shared.IOSMediaPlayerViewModel
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.util.core.PlatformType
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import coil3.PlatformContext as CoilPlatformContext

actual val platformModule = module {
	single { PlatformType.IOS }
	single<CacheDatabase> {
		val dbPath = documentDirectory() + "/cache.db"
		Room
			.databaseBuilder<CacheDatabase>(dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single<DownloadDatabase> {
		val dbPath = documentDirectory() + "/downloads.db"
		Room
			.databaseBuilder<DownloadDatabase>(dbPath)
			.setDriver(BundledSQLiteDriver())
			.fallbackToDestructiveMigration(true)
			.build()
	}

	single<MediaPlayerViewModel> {
		IOSMediaPlayerViewModel(
			stateRepository = get(),
			songRepository = get(),
			downloadManager = get(),
			connectivityManager = get(),
			syncManager = get(),
			sessionManager = get(),
			preferenceManager = get(),
			snackBarManager = get()
		)
	}

	singleOf(::ShareManager)
	single<CoilPlatformContext> { CoilPlatformContext.INSTANCE }
	singleOf(::StorageManager)
	singleOf(::ConnectivityManager)
	singleOf(::LogManager)
	singleOf(::AppIconManager)
	singleOf(::PermissionManager)
	singleOf(::LinkManager)
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

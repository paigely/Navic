package paige.navic.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import paige.navic.data.database.CacheDatabase
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.ShareManager
import paige.navic.shared.JvmMediaPlayerViewModel
import paige.navic.shared.MediaPlayerViewModel
import java.io.File

actual val platformModule = module {
	single<CacheDatabase> {
		val dbPath = File(
			System.getProperty("user.home"),
			"cache.db"
		).absolutePath
		Room
			.databaseBuilder<CacheDatabase>(dbPath)
			.setDriver(BundledSQLiteDriver())
			.build()
	}

	single<PlayerStateRepository> {
		val producePath = {
			val home = System.getProperty("user.home")
			val os = System.getProperty("os.name").lowercase()
			val directory = when {
				os.contains("linux") -> {
					val xdgConfig = System.getenv("XDG_CONFIG_HOME")
					if (!xdgConfig.isNullOrBlank()) {
						File(xdgConfig, "navic")
					} else {
						File(home, ".config/navic")
					}
				}
				os.contains("mac") -> File(home, "Library/Application Support/Navic")
				os.contains("win") -> File(System.getenv("AppData"), "Navic")
				else -> File(home, ".navic")
			}
			if (!directory.exists()) directory.mkdirs()
			File(directory, PlayerStateRepository.DATASTORE_FILE_NAME).absolutePath
		}
		PlayerStateRepository(PlayerStateRepository.getInstance(producePath))
	}

	viewModel<MediaPlayerViewModel> {
		JvmMediaPlayerViewModel(
			stateRepository = get(),
			trackRepository = get(),
			downloadManager = get(),
			connectivityManager = get()
		)
	}

	singleOf(::ShareManager)
	single<ConnectivityManager> { ConnectivityManager(get()) }
}

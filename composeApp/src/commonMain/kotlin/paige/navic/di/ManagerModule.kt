package paige.navic.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import paige.navic.data.database.SyncManager
import paige.navic.managers.DownloadManager

val managerModule = module {
	single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
	single<DownloadManager> { DownloadManager(get(), get(), get(), get(), get(), get()) }
	single(createdAtStart = true) {
		SyncManager(get(), get(), get(), get(), get()).apply {
			startPeriodicSync()
		}
	}
}

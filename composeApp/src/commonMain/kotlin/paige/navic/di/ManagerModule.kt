package paige.navic.di

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import paige.navic.domain.manager.SyncManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.manager.DownloadManager
import paige.navic.domain.manager.SleepTimerManager

val managerModule = module {
	single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
	single<DownloadManager> { DownloadManager(get(), get(), get(), get(), get(), get(), get(), get()) }
	single<SleepTimerManager> { SleepTimerManager(get(), get()) }
	single(createdAtStart = true) {
		SyncManager(get(), get(), get(), get(), get(), get()).apply {
			startPeriodicSync()
		}
	}
	singleOf(::SessionManager)
}

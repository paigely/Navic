package paige.navic.domain.manager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.util.core.Logger

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val syncManager: SyncManager by inject()

    override suspend fun doWork(): Result {
        Logger.i("SyncWorker", "Starting background sync work...")
        return try {
            syncManager.runSyncCycleFromWorker()
            Result.success()
        } catch (e: Exception) {
            Logger.e("SyncWorker", "Background sync failed", e)
            Result.retry()
        }
    }
}

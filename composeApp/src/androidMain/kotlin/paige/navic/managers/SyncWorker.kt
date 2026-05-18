package paige.navic.managers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.data.database.SyncManager

class SyncWorker(
	context: Context,
	params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

	private val syncManager: SyncManager by inject()

	override suspend fun doWork(): Result {
		return try {
			syncManager.runSyncCycleInternal()
			Result.success()
		} catch (e: Exception) {
			Result.retry()
		}
	}
}

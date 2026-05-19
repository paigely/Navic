package paige.navic.managers

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.data.database.SyncManager
import paige.navic.shared.Logger
import java.util.concurrent.TimeUnit
import kotlin.getValue

actual class SyncScheduler : KoinComponent {
	private val context: Context by inject()

	actual fun schedulePeriodicSync() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.UNMETERED)
			.setRequiresBatteryNotLow(true)
			.build()

		val syncRequest = PeriodicWorkRequestBuilder<Worker>(TASK_INTERVAL, TimeUnit.MINUTES)
			.setConstraints(constraints)
			.build()

		val instance = WorkManager.getInstance(context)
		instance.enqueueUniquePeriodicWork(
			TASK_IDENTIFIER,
			ExistingPeriodicWorkPolicy.KEEP,
			syncRequest
		)
	}

	class Worker(
		context: Context,
		params: WorkerParameters
	) : CoroutineWorker(context, params), KoinComponent {
		private val syncManager: SyncManager by inject()

		override suspend fun doWork(): Result {
			return try {
				syncManager.runSyncCycleInternal()
				Result.success()
			} catch (e: Exception) {
				Logger.e("SyncScheduler.Worker", "error in worker", e)
				Result.retry()
			}
		}
	}

	companion object {
		private const val TASK_INTERVAL = 15L
		private const val TASK_IDENTIFIER = "NavicSyncWork"
	}
}

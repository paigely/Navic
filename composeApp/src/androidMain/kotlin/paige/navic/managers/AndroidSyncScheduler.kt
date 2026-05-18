package paige.navic.managers

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import paige.navic.data.database.SyncScheduler
import java.util.concurrent.TimeUnit

class AndroidSyncScheduler(private val context: Context) : SyncScheduler {
	override fun schedulePeriodicSync() {
		val constraints = Constraints.Builder()
			.setRequiredNetworkType(NetworkType.CONNECTED)
			.build()

		val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
			.setConstraints(constraints)
			.build()

		WorkManager.getInstance(context).enqueueUniquePeriodicWork(
			"NavicSyncWork",
			ExistingPeriodicWorkPolicy.KEEP,
			syncRequest
		)
	}
}

@file:OptIn(ExperimentalForeignApi::class)

package paige.navic.managers

import kotlinx.cinterop.ExperimentalForeignApi
import paige.navic.data.database.SyncManager
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import paige.navic.shared.Logger
import paige.navic.utils.executeWithErrorHandling
import platform.BackgroundTasks.BGProcessingTaskRequest
import platform.BackgroundTasks.BGTask

// loosely based on https://thomaskioko.me/posts/kmp_background_tasks/

actual class SyncScheduler : KoinComponent {
	private val syncManager: SyncManager by inject()
	private val taskScheduler by lazy { BGTaskScheduler.sharedScheduler }
	private val scope = CoroutineScope(Dispatchers.Default)

	actual fun schedulePeriodicSync() {
		taskScheduler.registerForTaskWithIdentifier(
			identifier = TASK_IDENTIFIER,
			usingQueue = null,
			launchHandler = ::handleTask
		)
		requestScheduledTask()
	}

	private fun requestScheduledTask() {
		try {
			executeWithErrorHandling { errorPointer ->
				val request = BGProcessingTaskRequest(TASK_IDENTIFIER)
				request.earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(TASK_INTERVAL)
				taskScheduler.submitTaskRequest(request, errorPointer)
			}
		} catch (e: Exception) {
			Logger.e("SyncScheduler", "submitTaskRequest errored", e)
		}
	}

	private fun handleTask(task: BGTask?) {
		if (task == null) return
		scope.launch {
			try {
				syncManager.runSyncCycleInternal()
				task.setTaskCompletedWithSuccess(true)
			} catch (e: Exception) {
				Logger.e("SyncScheduler", "error in background loop", e)
				task.setTaskCompletedWithSuccess(false)
			}
		}
		requestScheduledTask()
	}

	companion object {
		private const val TASK_INTERVAL = 15.0 * 60.0
		private const val TASK_IDENTIFIER = "paige.Navic.refresh"
	}
}

package paige.navic.managers

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import paige.navic.data.database.SyncScheduler
import paige.navic.data.database.SyncManager
import platform.BackgroundTasks.BGTaskScheduler
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGAppRefreshTask
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSinceNow
import platform.Foundation.NSError
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IOSSyncScheduler : SyncScheduler, KoinComponent {

	// Lazily fetch the SyncManager via your existing Koin setup
	private val syncManager: SyncManager by inject()

	// Define your unique iOS background identifier matching Xcode configurations
	private val taskIdentifier = "paige.navic.refresh"

	// Keep a dedicated background scope for execution safety
	private val backgroundScope = CoroutineScope(Dispatchers.Default)

	override fun schedulePeriodicSync() {
		// 1. Register the launcher callback with iOS (Crucial: Must happen during app boot sequence)
		val registrationSuccess = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
			identifier = taskIdentifier,
			usingQueue = null
		) { task ->
			if (task is BGAppRefreshTask) {
				handleBackgroundSync(task)
			}
		}

		if (!registrationSuccess) {
			println("IOSSyncScheduler: Failed to register task handler for $taskIdentifier. Check setup order.")
		}

		// 2. Queue up the first actual background loop request
		submitAppRefreshRequest()
	}

	@OptIn(ExperimentalForeignApi::class)
	private fun submitAppRefreshRequest() {
		val request = BGAppRefreshTaskRequest(taskIdentifier).apply {
			// Advise iOS to wait at least 15 minutes (900 seconds) before considering execution
			earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(900.0)
		}

		try {
			val error: NSError? = null
			val success = BGTaskScheduler.sharedScheduler.submitTaskRequest(request,
				error as CPointer<ObjCObjectVar<NSError?>>?
			)
			if (!success || error != null) {
				println("IOSSyncScheduler: Failed to submit request: ${error?.localizedDescription}")
			}
		} catch (e: Exception) {
			println("IOSSyncScheduler: Exception while submitting task: ${e.message}")
		}
	}

	private fun handleBackgroundSync(task: BGAppRefreshTask) {
		// IMMEDIATELY schedule the next execution block before processing this one
		submitAppRefreshRequest()

		// Handle early graceful termination if iOS cuts the task's execution short
		task.expirationHandler = {
			task.setTaskCompletedWithSuccess(false)
		}

		// Execute your core sync cycle suspend method
		backgroundScope.launch {
			try {
				syncManager.runSyncCycleInternal()
				task.setTaskCompletedWithSuccess(true)
			} catch (e: Exception) {
				println("IOSSyncScheduler: Error during background loop execution: ${e.message}")
				task.setTaskCompletedWithSuccess(false)
			}
		}
	}
}

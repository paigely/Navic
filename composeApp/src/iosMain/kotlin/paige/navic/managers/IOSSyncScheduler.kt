package paige.navic.managers

import paige.navic.data.database.SyncScheduler

class IOSSyncScheduler : SyncScheduler {
	override fun schedulePeriodicSync() {
		// iOS implementation using BackgroundTasks framework could go here
	}
}

package paige.navic.managers

import org.koin.core.component.KoinComponent

expect class SyncScheduler : KoinComponent {
	fun schedulePeriodicSync()
}

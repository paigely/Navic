package paige.navic.domain.manager

interface BackgroundScheduler {
    fun schedulePeriodicSync(intervalHours: Long)
    fun cancelPeriodicSync()
}

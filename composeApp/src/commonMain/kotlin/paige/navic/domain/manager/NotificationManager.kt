package paige.navic.domain.manager

expect class NotificationManager {
	fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean = false
	)

	fun cancelNotification(id: Int)

	fun requestPermissions()
}

object NotificationIds {
	const val DOWNLOAD_LIBRARY = 1001
	const val SYNC_LIBRARY = 1002
}

package paige.navic.domain.manager

actual class NotificationManager {
	actual fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	) {
	}

	actual fun cancelNotification(id: Int) {
	}

	actual fun requestPermissions() {
	}
}

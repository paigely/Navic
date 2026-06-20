package paige.navic.domain.manager

import paige.navic.util.core.Logger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionSound

actual class NotificationManager {
	private val notificationCenter = UNUserNotificationCenter.currentNotificationCenter()

	init {
		notificationCenter.requestAuthorizationWithOptions(
			UNAuthorizationOptionAlert or UNAuthorizationOptionSound
		) { granted, error ->
			if (error != null) {
				Logger.e("NotificationManager", "Failed to request notification authorization", Exception(error.localizedDescription))
			} else if (!granted) {
				Logger.w("NotificationManager", "Notification permission was denied by the user", null)
			} else {
				Logger.i("NotificationManager", "Notification permission granted", null)
			}
		}
	}

	actual fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	) {
		val content = UNMutableNotificationContent().apply {
			setTitle(title)
			setBody(if (indeterminate) message else "$message (${(progress * 100).toInt()}%)")
		}

		val request = UNNotificationRequest.requestWithIdentifier(
			identifier = id.toString(),
			content = content,
			trigger = null
		)

		notificationCenter.addNotificationRequest(request) { error ->
			if (error != null) {
				Logger.e("NotificationManager", "Failed to show notification $id", Exception(error.localizedDescription))
			}
		}
	}

	actual fun cancelNotification(id: Int) {
		notificationCenter.removeDeliveredNotificationsWithIdentifiers(listOf(id.toString()))
	}

	actual fun requestPermissions() {
		notificationCenter.requestAuthorizationWithOptions(
			UNAuthorizationOptionAlert or UNAuthorizationOptionSound
		) { granted, error ->
			if (error != null) {
				Logger.e("NotificationManager", "Failed to request notification authorization", Exception(error.localizedDescription))
			} else if (!granted) {
				Logger.w("NotificationManager", "Notification permission was denied by the user", null)
			} else {
				Logger.i("NotificationManager", "Notification permission granted", null)
			}
		}
	}
}

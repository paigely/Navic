package paige.navic.domain.manager

import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import paige.navic.util.core.ResourceProvider

actual class NotificationManager(
	private val context: Context,
	private val resourceProvider: ResourceProvider
) {
	private val notificationManager =
		context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager

	init {
		createNotificationChannel()
	}

	private fun createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val channel = NotificationChannel(
				CHANNEL_ID,
				"Library Updates",
				AndroidNotificationManager.IMPORTANCE_LOW
			).apply {
				description = "Notifications for library sync and downloads"
			}
			notificationManager.createNotificationChannel(channel)
		}
	}

	actual fun showProgressNotification(
		id: Int,
		title: String,
		message: String,
		progress: Float,
		indeterminate: Boolean
	) {
		val builder = NotificationCompat.Builder(context, CHANNEL_ID)
			.setSmallIcon(resourceProvider.icNavic)
			.setContentTitle(title)
			.setContentText(message)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setOngoing(true)
			.setOnlyAlertOnce(true)
			.setProgress(100, (progress * 100).toInt(), indeterminate)

		notificationManager.notify(id, builder.build())
	}

	actual fun cancelNotification(id: Int) {
		notificationManager.cancel(id)
	}

	companion object {
		private const val CHANNEL_ID = "library_updates"
	}
}

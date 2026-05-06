package paige.navic.androidApp.widgets

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import paige.navic.shared.PlaybackService

class WidgetPlaybackAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		val action = parameters[actionKey] ?: return

		val serviceIntent = Intent(context, PlaybackService::class.java).apply {
			this.action = action
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(serviceIntent)
		} else {
			context.startService(serviceIntent)
		}
	}

	companion object {
		val actionKey = ActionParameters.Key<String>("playback_action")
		const val ACTION_PLAY_PAUSE = PlaybackService.ACTION_PLAY_PAUSE
		const val ACTION_NEXT = PlaybackService.ACTION_NEXT
		const val ACTION_PREV = PlaybackService.ACTION_PREV
	}
}

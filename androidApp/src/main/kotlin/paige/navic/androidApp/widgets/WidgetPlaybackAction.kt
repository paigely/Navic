package paige.navic.androidApp.widgets

import android.content.ComponentName
import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import paige.navic.shared.PlaybackService

class WidgetPlaybackAction : ActionCallback {
	override suspend fun onAction(
		context: Context,
		glanceId: GlanceId,
		parameters: ActionParameters
	) {
		val action = parameters[actionKey] ?: return

		withContext(Dispatchers.Main) {
			val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
			val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()

			try {
				val controller = withContext(Dispatchers.IO) { controllerFuture.get() }

				when (action) {
					ACTION_PLAY_PAUSE -> {
						if (controller.isPlaying) controller.pause() else controller.play()
					}
					ACTION_NEXT -> controller.seekToNextMediaItem()
					ACTION_PREV -> controller.seekToPreviousMediaItem()
				}

				delay(200)

			} catch (e: Exception) {
				e.printStackTrace()
			} finally {
				MediaController.releaseFuture(controllerFuture)
			}
		}
	}

	companion object {
		val actionKey = ActionParameters.Key<String>("playback_action")
		const val ACTION_PLAY_PAUSE = "play_pause"
		const val ACTION_NEXT = "next"
		const val ACTION_PREV = "prev"
		const val ACTION_STAR = "star"
	}
}

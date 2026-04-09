package paige.navic.androidApp.widgets.nowplaying

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Base receiver class which widgets' receivers will inherit from. Used with `NowPlayingWidget`
 */
open class NowPlayingReceiver(
	private val widgetClass: Class<out NowPlayingWidget>
) : GlanceAppWidgetReceiver() {

	override val glanceAppWidget: GlanceAppWidget by lazy {
		widgetClass.getDeclaredConstructor().newInstance()
	}

	override fun onReceive(context: Context, intent: Intent) {
		super.onReceive(context, intent)

		val isPlaying = intent.getBooleanExtra("isPlaying", false)
		val title = intent.getStringExtra("title") ?: ""
		val artist = intent.getStringExtra("artist") ?: ""
		val artUrl = intent.getStringExtra("artUrl")

		val pendingResult = goAsync()

		MainScope().launch {
			try {
				val glanceIds = GlanceAppWidgetManager(context).getGlanceIds(widgetClass)
				glanceIds.forEach { id ->
					updateAppWidgetState(context, id) { prefs ->
						prefs[NowPlayingKeys.isPlaying] = isPlaying
						prefs[NowPlayingKeys.titleKey] = title
						prefs[NowPlayingKeys.artistKey] = artist
						prefs[NowPlayingKeys.artUrlKey] = artUrl ?: ""
					}
					glanceAppWidget.update(context, id)
				}
			} finally {
				runCatching { pendingResult.finish() }
			}
		}
	}
}

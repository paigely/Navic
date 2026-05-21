package paige.navic.androidApp.widgets.nowplaying

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.KeyEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import paige.navic.androidApp.MainActivity
import androidx.core.net.toUri

/**
 * Base widgets class which widgets will inherit from. Used with `NowPlayingReceiver`
 */
abstract class NowPlayingWidget : GlanceAppWidget() {
	override val stateDefinition = PreferencesGlanceStateDefinition

	override suspend fun provideGlance(context: Context, id: GlanceId) {
		provideContent {
			val prefs = currentState<Preferences>()

			val isPlaying = prefs[NowPlayingKeys.isPlaying] ?: false
			val title = prefs[NowPlayingKeys.titleKey]?.takeIf { it.isNotBlank() } ?: "Not Playing"
			val artist = prefs[NowPlayingKeys.artistKey]?.takeIf { it.isNotBlank() } ?: "No Artist"
			val artUrl = prefs[NowPlayingKeys.artUrlKey]

			val bitmap by produceState<Bitmap?>(initialValue = null, artUrl) {
				value = fetchBitmap(context, artUrl)
			}

			GlanceTheme {
				Content(
					context = context,
					isPlaying = isPlaying,
					title = title,
					artist = artist,
					bitmap = bitmap
				)
			}
		}
	}

	@Composable
	abstract fun Content(
		context: Context,
		isPlaying: Boolean,
		title: String,
		artist: String,
		bitmap: Bitmap?
	)

	/**
	 * Used to send pause/play/skip events.
	 *
	 * e.g. `.clickable(actionSendBroadcast(createMediaIntent(context, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)))`
	 */
	protected fun createMediaIntent(context: Context, keyCode: Int) =
		Intent(Intent.ACTION_MEDIA_BUTTON).apply {
			setPackage(context.packageName)
			component = ComponentName(context, "androidx.media3.session.MediaButtonReceiver")
			putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
		}

	protected fun launchIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
		action = Intent.ACTION_MAIN
		addCategory(Intent.CATEGORY_LAUNCHER)
		addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
	}

	private suspend fun fetchBitmap(context: Context, url: String?): Bitmap? {
		if (url == null) return null

		val uri = url.toUri()
		val cacheKey = uri.getQueryParameter("cacheKey")

		val request = ImageRequest.Builder(context)
			.data(url)
			.apply {
				cacheKey?.let { key ->
					memoryCacheKey(key)
					diskCacheKey(key)
				}
			}
			.size(700)
			.allowHardware(false)
			.build()
		return (context.imageLoader.execute(request) as? SuccessResult)?.image?.toBitmap()
	}
}

package paige.navic.domain.manager

import android.app.Application
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import paige.navic.util.core.Logger

actual class LinkManager(
	private val application: Application
) {
	actual fun openLink(link: String) {
		val intent = CustomTabsIntent.Builder()
			.setEphemeralBrowsingEnabled(true)
			.setShareState(CustomTabsIntent.SHARE_STATE_OFF)
			.build()
		intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		try {
			intent.launchUrl(application, link.toUri())
		} catch (ex: android.content.ActivityNotFoundException) {
			Logger.w("LinkManager", "no browser found", ex)
			Toast.makeText(
				application.applicationContext,
				"You don't have a browser..!",
				Toast.LENGTH_SHORT
			).show()
		} catch (ex: Exception) {
			Logger.e("LinkManager", "couldn't launch intent somehow", ex)
			Toast.makeText(
				application.applicationContext,
				"Error: ${ex.localizedMessage}",
				Toast.LENGTH_SHORT
			).show()
		}
	}
}

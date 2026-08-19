package paige.navic.domain.manager

import android.app.Application
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri

actual class LinkManager(
	private val application: Application
) {
	actual fun openLink(link: String) {
		val intent = CustomTabsIntent.Builder()
			.setEphemeralBrowsingEnabled(true)
			.setShareState(CustomTabsIntent.SHARE_STATE_OFF)
			.build()
		intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		intent.launchUrl(application, link.toUri())
	}
}

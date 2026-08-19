package paige.navic.domain.manager

import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.SafariServices.SFSafariViewControllerConfiguration
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationPageSheet
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene

actual class LinkManager {

	/**
	 * utility to try and get the top view controller
	 *
	 * @return
	 */
	private fun getTopVC(): UIViewController? {
		val window = UIApplication.sharedApplication.connectedScenes
			.filterIsInstance<UIWindowScene>()
			.flatMap { it.windows }
			.filterIsInstance<UIWindow>()
			.firstOrNull { it.isKeyWindow() }
			?: return null
		var rootViewController = window.rootViewController
		while (rootViewController?.presentedViewController != null) {
			rootViewController = rootViewController.presentedViewController
		}
		return rootViewController
	}

	actual fun openLink(link: String) {
		val rootViewController = getTopVC()

		val safariConfiguration = SFSafariViewControllerConfiguration()
		safariConfiguration.entersReaderIfAvailable = false

		val safariViewController = SFSafariViewController(
			uRL = NSURL.URLWithString(link)!!,
			configuration = safariConfiguration
		)

		safariViewController.modalPresentationStyle = UIModalPresentationPageSheet
		rootViewController?.presentViewController(safariViewController, true, null)
	}
}

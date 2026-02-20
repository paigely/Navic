package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice
import platform.UIKit.UIImage
import platform.UIKit.UIUserInterfaceIdiomPad
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

@OptIn(ExperimentalForeignApi::class)
actual class ShareManager {

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

	/**
	 * utility to share a list of anything
	 *
	 * @param activityItems
	 */
	private fun share(vararg activityItems: Any) {
		val rootViewController = getTopVC()
		val activityViewController = UIActivityViewController(activityItems.asList(), null)

		// will crash on iPadOS if you don't do this
		if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
			activityViewController.popoverPresentationController?.sourceView =
				rootViewController?.view
		}

		rootViewController?.presentViewController(activityViewController, true, null)
	}

	actual suspend fun shareImage(bitmap: ImageBitmap, fileName: String) {
		val imageBytes = bitmap.asSkiaBitmap().readPixels() ?: return
		val data = imageBytes.usePinned { pinned ->
			NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage.imageWithData(data) ?: return
		share(image)
	}

	actual suspend fun shareString(string: String) = share(string)
}

@Composable
actual fun rememberShareManager(): ShareManager = remember { ShareManager() }

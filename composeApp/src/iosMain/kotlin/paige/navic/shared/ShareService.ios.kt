package paige.navic.shared

import kotlinx.cinterop.*
import platform.UIKit.*
import platform.Foundation.*

class IOSShareService : ShareService {
	@OptIn(ExperimentalForeignApi::class)
	override fun shareImage(imageBytes: ByteArray, fileName: String) {
		val data = imageBytes.usePinned { pinned ->
			NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage.imageWithData(data) ?: return

		val window = UIApplication.sharedApplication.windows.firstOrNull { (it as UIWindow).isKeyWindow() } as? UIWindow
		var rootViewController = window?.rootViewController
		while (rootViewController?.presentedViewController != null) {
			rootViewController = rootViewController.presentedViewController
		}

		val activityViewController = UIActivityViewController(listOf(image), null)

		// Fix for iPad crashes
		if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
			activityViewController.popoverPresentationController?.sourceView = rootViewController?.view
		}

		rootViewController?.presentViewController(activityViewController, true, null)
	}
}
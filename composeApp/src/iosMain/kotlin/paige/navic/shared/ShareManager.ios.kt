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
import platform.UIKit.UIWindow
import platform.UIKit.popoverPresentationController

actual class ShareManager {
	@OptIn(ExperimentalForeignApi::class)
	actual suspend fun shareImage(bitmap: ImageBitmap, fileName: String) {
		val imageBytes = bitmap.asSkiaBitmap().readPixels() ?: return
		val data = imageBytes.usePinned { pinned ->
			NSData.dataWithBytes(pinned.addressOf(0), imageBytes.size.toULong())
		}
		val image = UIImage.imageWithData(data) ?: return

		val window =
			UIApplication.sharedApplication.windows.firstOrNull { (it as UIWindow).isKeyWindow() } as? UIWindow
		var rootViewController = window?.rootViewController
		while (rootViewController?.presentedViewController != null) {
			rootViewController = rootViewController.presentedViewController
		}

		val activityViewController = UIActivityViewController(listOf(image), null)

		// will crash on iPadOS if you don't do this
		if (UIDevice.currentDevice.userInterfaceIdiom == UIUserInterfaceIdiomPad) {
			activityViewController.popoverPresentationController?.sourceView =
				rootViewController?.view
		}

		rootViewController?.presentViewController(activityViewController, true, null)
	}
}

@Composable
actual fun rememberShareManager(): ShareManager = remember { ShareManager() }

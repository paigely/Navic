package paige.navic.util.core

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSBundle
import platform.UIKit.UIDevice

@OptIn(
	ExperimentalMaterial3WindowSizeClassApi::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
actual fun rememberPlatformContext(): PlatformContext {
	val sizeClass = calculateWindowSizeClass()
	return remember {
		object : PlatformContext {
			override fun clickSound() {
				// none for iOS
			}

			override fun checkLocalNetworkPermission() {
				// none for iOS
			}

			override val name = (UIDevice.currentDevice.systemName()
				+ " " + UIDevice.currentDevice.systemVersion)
			override val appVersion: String =
				(NSBundle.mainBundle.objectForInfoDictionaryKey(
					"CFBundleShortVersionString"
				) as? String).toString()
			override val colorScheme = null
			override val sizeClass = sizeClass
		}
	}
}

actual fun <T> synchronized(lock: Any, block: () -> T): T = block()

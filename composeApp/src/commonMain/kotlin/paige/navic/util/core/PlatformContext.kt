package paige.navic.util.core

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable

interface PlatformContext {
	val name: String
	val appVersion: String
	val colorScheme: ColorScheme?
	val sizeClass: WindowSizeClass
	val platformType: PlatformType
	fun checkLocalNetworkPermission()
	fun clickSound()
}

enum class PlatformType {
	Android,
	IOS
}

@Composable
expect fun rememberPlatformContext(): PlatformContext

expect fun <T> synchronized(lock: Any, block: () -> T): T

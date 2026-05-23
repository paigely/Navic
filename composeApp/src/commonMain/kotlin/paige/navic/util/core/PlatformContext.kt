package paige.navic.util.core

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable

interface PlatformContext {
	val name: String
	val appVersion: String
	val colorScheme: ColorScheme?
	val sizeClass: WindowSizeClass
	fun clickSound()
}

@Composable
expect fun rememberPlatformContext(): PlatformContext

expect fun <T> synchronized(lock: Any, block: () -> T): T

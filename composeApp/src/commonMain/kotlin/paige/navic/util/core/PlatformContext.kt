package paige.navic.util.core

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

interface PlatformContext {
	val name: String
	val appVersion: String
	val colorScheme: ColorScheme?
	val sizeClass: WindowSizeClass
	val platformType: PlatformType
}

@Composable
fun PlatformContext.isLandscape() = remember(sizeClass) {
	sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
}

enum class PlatformType {
	Android,
	IOS
}

@Composable
expect fun rememberPlatformContext(): PlatformContext

expect fun <T> synchronized(lock: Any, block: () -> T): T

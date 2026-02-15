package paige.navic.shared

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

interface Ctx {
	val name: String
	val appVersion: String
	val colorScheme: ColorScheme?
	val sizeClass: WindowSizeClass
	fun clickSound()
}

@Composable
expect fun rememberCtx(): Ctx

@Composable
expect fun Modifier.systemGesturesExclusion(): Modifier

package paige.navic

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable

interface Ctx {
	val name: String
	val colorScheme: ColorScheme
	val sizeClass: WindowSizeClass
	fun clickSound()
}

@Composable
expect fun rememberCtx(): Ctx

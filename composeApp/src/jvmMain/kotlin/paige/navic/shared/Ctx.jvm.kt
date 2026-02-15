package paige.navic.shared

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@OptIn(
	ExperimentalMaterial3WindowSizeClassApi::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
actual fun rememberCtx(): Ctx {
	val sizeClass = calculateWindowSizeClass()
	return remember(sizeClass) {
		object : Ctx {
			override fun clickSound() {
				// none for jvm
			}
			override val name = "Desktop (Java ${System.getProperty("java.version")})"
			override val appVersion: String = System.getProperty("jpackage.app-version") ?: "unknown version"
			override val colorScheme = null
			override val sizeClass = sizeClass
		}
	}
}

@Composable
actual fun Modifier.systemGesturesExclusion(): Modifier = this

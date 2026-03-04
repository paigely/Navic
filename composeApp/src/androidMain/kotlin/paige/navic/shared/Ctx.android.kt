package paige.navic.shared

import android.os.Build
import android.view.SoundEffectConstants
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

@OptIn(
	ExperimentalMaterial3WindowSizeClassApi::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
actual fun rememberCtx(): Ctx {
	val view = LocalView.current
	val context = LocalContext.current
	val darkTheme = isSystemInDarkTheme()
	val sizeClass = calculateWindowSizeClass(LocalActivity.current!!)
	return remember {
		object : Ctx {
			override fun clickSound() {
				view.playSoundEffect(SoundEffectConstants.CLICK)
			}
			override val name = "Android ${Build.VERSION.SDK_INT}"
			override val appVersion: String =
				context.packageManager
					.getPackageInfo(context.packageName, 0)
					.versionName.toString()
			override val colorScheme
				get() = if (Build.VERSION.SDK_INT >= 31)
					if (darkTheme)
						dynamicDarkColorScheme(context)
					else dynamicLightColorScheme(context)
				else
					if (darkTheme)
						darkColorScheme()
					else expressiveLightColorScheme()
			override val sizeClass = sizeClass
		}
	}
}

@Composable
actual fun Modifier.systemGesturesExclusion(): Modifier
	= this.systemGestureExclusion()

actual fun <T> synchronized(lock: Any, block: () -> T): T = kotlin.synchronized(lock, block)
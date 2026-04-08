package paige.navic.shared

import android.app.Activity
import android.os.Build
import android.view.SoundEffectConstants
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.ThemeMode

@OptIn(
	ExperimentalMaterial3WindowSizeClassApi::class,
	ExperimentalMaterial3ExpressiveApi::class
)
@Composable
actual fun rememberCtx(): Ctx {
	val view = LocalView.current
	val context = LocalContext.current
	val inDarkTheme = isSystemInDarkTheme()
	val isDark = remember(Settings.shared.themeMode) {
		when (Settings.shared.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val sizeClass = calculateWindowSizeClass(LocalActivity.current!!)
	SideEffect {
		(view.context as? Activity)?.window?.let { window ->
			WindowCompat.getInsetsController(window, view)
				.isAppearanceLightStatusBars = !isDark
		}
	}
	return remember(isDark, sizeClass) {
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
					if (isDark)
						dynamicDarkColorScheme(context)
					else dynamicLightColorScheme(context)
				else
					if (isDark)
						darkColorScheme()
					else expressiveLightColorScheme()
			override val sizeClass = sizeClass
		}
	}
}

actual fun <T> synchronized(lock: Any, block: () -> T): T = kotlin.synchronized(lock, block)

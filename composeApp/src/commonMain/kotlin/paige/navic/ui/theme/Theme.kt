package paige.navic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.runtime.Composable
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.zt64.compose.pipette.HsvColor
import paige.navic.LocalCtx
import paige.navic.data.models.Settings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavicTheme(
	colorScheme: ColorScheme? = null,
	forceColorScheme: Boolean = false,
	content: @Composable () -> Unit
) {
	val ctx = LocalCtx.current
	val colorScheme = @Composable {
		if (!Settings.shared.dynamicColour && !forceColorScheme) {
			rememberDynamicColorScheme(
				seedColor = HsvColor(
					Settings.shared.accentColourH,
					Settings.shared.accentColourS,
					Settings.shared.accentColourV
				).toColor(),
				isDark = isSystemInDarkTheme(),
				specVersion = ColorSpec.SpecVersion.SPEC_2025,
			)
		} else {
			colorScheme ?: ctx.colorScheme
		}
	}

	MaterialExpressiveTheme(
		motionScheme = MotionScheme.expressive(),
		colorScheme = colorScheme(),
		typography = if (Settings.shared.useSystemFont)
			MaterialTheme.typography
		else typography(),
		content = content
	)
}

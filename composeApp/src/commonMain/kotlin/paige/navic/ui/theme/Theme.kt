package paige.navic.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import dev.burnoo.compose.remembersetting.rememberFloatSetting
import dev.zt64.compose.pipette.HsvColor
import paige.navic.LocalCtx

@Composable
fun NavicTheme(
	colorScheme: ColorScheme? = null,
	forceColorScheme: Boolean = false,
	content: @Composable () -> Unit
) {
	val ctx = LocalCtx.current
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)
	var dynamicColour by rememberBooleanSetting("dynamicColour", true)
	var accentColourH by rememberFloatSetting("accentColourH", 0f)
	var accentColourS by rememberFloatSetting("accentColourS", 0f)
	var accentColourV by rememberFloatSetting("accentColourV", 1f)

	val colorScheme = @Composable {
		if (!dynamicColour && !forceColorScheme) {
			rememberDynamicColorScheme(
				seedColor = HsvColor(accentColourH, accentColourS, accentColourV).toColor(),
				isDark = isSystemInDarkTheme(),
				specVersion = ColorSpec.SpecVersion.SPEC_2025,
			)
		} else {
			colorScheme ?: ctx.colorScheme
		}
	}

	MaterialTheme(
		colorScheme = colorScheme(),
		typography = if (useSystemFont)
			MaterialTheme.typography
		else typography(),
		content = content
	)
}

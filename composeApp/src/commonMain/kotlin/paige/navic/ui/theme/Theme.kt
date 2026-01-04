package paige.navic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import paige.navic.LocalCtx

@Composable
fun NavicTheme(
	colorScheme: ColorScheme? = null,
	content: @Composable () -> Unit
) {
	val ctx = LocalCtx.current
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)

	MaterialTheme(
		colorScheme = colorScheme ?: ctx.colorScheme,
		typography = if (useSystemFont)
			MaterialTheme.typography
		else typography(),
		content = content
	)
}

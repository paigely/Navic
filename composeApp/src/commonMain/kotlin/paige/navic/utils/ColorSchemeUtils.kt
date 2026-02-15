package paige.navic.utils

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme

private val IosRed = Color(255, 66, 69)

// TODO: actually use UIColor values on iOS
@Composable
fun lightIosColorScheme(
	accent: Color
): ColorScheme {
	return rememberDynamicColorScheme(
		primary = Color.White,
		isDark = false,
		isAmoled = true,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		style = PaletteStyle.Content,
		modifyColorScheme = { scheme ->
			scheme.copy(
				primary = accent,
				onPrimary = Color.White,
				primaryContainer = accent.copy(alpha = .3f),
				onPrimaryContainer = accent,
				secondaryContainer = accent.copy(alpha = .3f),
				onSecondaryContainer = accent,
				secondary = accent,
				tertiaryContainer = accent.copy(alpha = .3f),
				onTertiaryContainer = accent,
				tertiary = accent,
				error = IosRed,
				onError = Color.White,
				errorContainer = IosRed,
				onErrorContainer = Color.White,
				surfaceVariant = Color(224, 221, 220)
			)
		}
	)
}

@Composable
fun darkIosColorScheme(
	accent: Color
): ColorScheme {
	return rememberDynamicColorScheme(
		primary = Color.White,
		isDark = true,
		isAmoled = true,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		style = PaletteStyle.Content,
		modifyColorScheme = { scheme ->
			scheme.copy(
				primary = accent,
				onPrimary = Color.White,
				primaryContainer = accent.copy(alpha = .3f),
				onPrimaryContainer = accent,
				secondaryContainer = accent.copy(alpha = .3f),
				onSecondaryContainer = accent,
				secondary = accent,
				tertiaryContainer = accent.copy(alpha = .3f),
				onTertiaryContainer = accent,
				tertiary = accent,
				error = IosRed,
				onError = Color.White,
				errorContainer = IosRed,
				onErrorContainer = Color.White,
				surfaceVariant = Color(44, 44, 46),
				onSurfaceVariant = Color(142, 142, 147)
			)
		}
	)
}

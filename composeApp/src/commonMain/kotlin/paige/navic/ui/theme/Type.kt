package paige.navic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.google_sans
import navic.composeapp.generated.resources.maple_mono
import org.jetbrains.compose.resources.Font

private val defaultTypography = Typography()

@Composable
fun mapleMono() = FontFamily(
	Font(Res.font.maple_mono)
)

@Composable
fun googleSans(
	grade: Int = 0,
	width: Float = 100f,
	round: Float = 0f
): FontFamily {
	return FontFamily(Font(
		Res.font.google_sans,
		variationSettings = FontVariation.Settings(
			FontVariation.grade(grade),
			FontVariation.width(width),
			FontVariation.Setting("ROND", round)
		)
	))
}

@Composable
fun typography(): Typography {
	val fontFamily = googleSans()
	return Typography(
		displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
		displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
		displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),

		headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
		headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
		headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),

		titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
		titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
		titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),

		bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
		bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
		bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),

		labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
		labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
		labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
	)
}

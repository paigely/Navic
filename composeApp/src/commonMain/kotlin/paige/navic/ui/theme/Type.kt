package paige.navic.ui.theme

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.google_sans
import navic.composeapp.generated.resources.maple_mono
import org.jetbrains.compose.resources.Font
import paige.navic.data.models.Settings

private val defaultTypography = Typography()

@Composable
fun mapleMono() = FontFamily(
	Font(Res.font.maple_mono)
)

@Composable
fun defaultFont(
	grade: Int = 0,
	width: Float = 100f,
	round: Float = 0f
): FontFamily {
	return if (Settings.shared.useSystemFont) {
		FontFamily.Default
	} else {
		FontFamily(Font(
			Res.font.google_sans,
			variationSettings = FontVariation.Settings(
				FontVariation.grade(grade),
				FontVariation.width(width),
				FontVariation.Setting("ROND", round)
			)
		))
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun typography(): Typography {
	val fontFamily = defaultFont()
	return Typography(
		displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
		displayLargeEmphasized = defaultTypography.displayLargeEmphasized.copy(fontFamily = fontFamily),
		displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
		displayMediumEmphasized = defaultTypography.displayMediumEmphasized.copy(fontFamily = fontFamily),
		displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
		displaySmallEmphasized = defaultTypography.displaySmallEmphasized.copy(fontFamily = fontFamily),

		headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
		headlineLargeEmphasized = defaultTypography.headlineLargeEmphasized.copy(fontFamily = fontFamily),
		headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
		headlineMediumEmphasized = defaultTypography.headlineMediumEmphasized.copy(fontFamily = fontFamily),
		headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
		headlineSmallEmphasized = defaultTypography.headlineSmallEmphasized.copy(fontFamily = fontFamily),

		titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
		titleLargeEmphasized = defaultTypography.titleLargeEmphasized.copy(fontFamily = fontFamily),
		titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
		titleMediumEmphasized = defaultTypography.titleMediumEmphasized.copy(fontFamily = fontFamily),
		titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
		titleSmallEmphasized = defaultTypography.titleSmallEmphasized.copy(fontFamily = fontFamily),

		bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
		bodyLargeEmphasized = defaultTypography.bodyLargeEmphasized.copy(fontFamily = fontFamily),
		bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
		bodyMediumEmphasized = defaultTypography.bodyMediumEmphasized.copy(fontFamily = fontFamily),
		bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
		bodySmallEmphasized = defaultTypography.bodySmallEmphasized.copy(fontFamily = fontFamily),

		labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
		labelLargeEmphasized = defaultTypography.labelLargeEmphasized.copy(fontFamily = fontFamily),
		labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
		labelMediumEmphasized = defaultTypography.labelMediumEmphasized.copy(fontFamily = fontFamily),
		labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily),
		labelSmallEmphasized = defaultTypography.labelSmallEmphasized.copy(fontFamily = fontFamily)
	)
}

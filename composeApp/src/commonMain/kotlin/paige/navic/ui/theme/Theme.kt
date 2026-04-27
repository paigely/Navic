package paige.navic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.AnimationStyle

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavicTheme(
	colorScheme: ColorScheme? = null,
	content: @Composable () -> Unit
) {
	val chosenTheme = Settings.shared.theme
	val chosenScheme = chosenTheme.colorScheme()
	val motionScheme = remember(Settings.shared.animationStyle) {
		when (Settings.shared.animationStyle) {
			AnimationStyle.Expressive -> MotionScheme.expressive()
			AnimationStyle.Standard -> MotionScheme.standard()
		}
	}
	MaterialExpressiveTheme(
		colorScheme = colorScheme
			?: chosenScheme,
		motionScheme = motionScheme,
		typography = typography(),
		shapes = Shapes(
			extraSmall = ContinuousRoundedRectangle(ShapeDefaults.ExtraSmall.topStart),
			small = ContinuousRoundedRectangle(ShapeDefaults.Small.topStart),
			medium = ContinuousRoundedRectangle(ShapeDefaults.Medium.topStart),
			large = ContinuousRoundedRectangle(ShapeDefaults.Large.topStart),
			extraLarge = ContinuousRoundedRectangle(ShapeDefaults.ExtraLarge.topStart),
			largeIncreased = ContinuousRoundedRectangle(ShapeDefaults.LargeIncreased.topStart),
			extraLargeIncreased = ContinuousRoundedRectangle(ShapeDefaults.ExtraLargeIncreased.topStart),
			extraExtraLarge = ContinuousRoundedRectangle(ShapeDefaults.ExtraExtraLarge.topStart)
		),
		content = content
	)
}

package paige.navic.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.data.models.Settings

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NavicTheme(
	colorScheme: ColorScheme? = null,
	content: @Composable () -> Unit
) {
	val chosenTheme = Settings.shared.theme
	val chosenScheme = chosenTheme.colorScheme()
	MaterialExpressiveTheme(
		colorScheme = colorScheme
			?: chosenScheme,
		motionScheme = MotionScheme.expressive(),
		typography = if (Settings.shared.useSystemFont)
			MaterialTheme.typography
		else typography(),
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

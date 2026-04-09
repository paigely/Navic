package paige.navic.androidApp.utils

import android.R
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius

/**
 * Applies corner radius for views that are visually positioned [widgetPadding]dp inside of the
 * widget background.
 */
@Composable
fun GlanceModifier.appWidgetInnerCornerRadius(widgetPadding: Dp): GlanceModifier {

	if (Build.VERSION.SDK_INT < 31) {
		return this
	}

	val resources = LocalContext.current.resources
	// get dimension in float (without rounding).
	val px = resources.getDimension(R.dimen.system_app_widget_background_radius)
	val widgetBackgroundRadiusDpValue = px / resources.displayMetrics.density
	if (widgetBackgroundRadiusDpValue < widgetPadding.value) {
		return this
	}
	return this.cornerRadius(Dp(widgetBackgroundRadiusDpValue - widgetPadding.value))
}

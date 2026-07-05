package paige.navic.domain.manager

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import paige.navic.domain.models.settings.AppIconVariant

actual class AppIconManager(
	private val context: Context,
	private val preferenceManager: PreferenceManager,
) {
	actual fun setVariant(newVariant: AppIconVariant) {
		preferenceManager.appIconVariant = newVariant
		AppIconVariant.entries.forEach { variant ->
			context.packageManager.setComponentEnabledSetting(
				ComponentName(
					context.packageName,
					"paige.navic.androidApp.${variant.activityName}"
				),
				if (variant == newVariant) {
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				} else {
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED
				},
				PackageManager.DONT_KILL_APP
			)
		}
	}
}

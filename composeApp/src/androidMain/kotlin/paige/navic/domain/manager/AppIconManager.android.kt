package paige.navic.domain.manager

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
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

	actual fun getIcon(variant: AppIconVariant): Any? {
		val resources = context.resources
		val iconName = when (variant) {
			AppIconVariant.Default -> "ic_launcher"
			AppIconVariant.Inverted -> "ic_launcher_inverted"
		}

		val iconRes = resources.getIdentifier(iconName, "mipmap", context.packageName)
		val drawable = if (iconRes != 0) {
			ResourcesCompat.getDrawable(resources, iconRes, null)
		} else {
			val activityName = "paige.navic.androidApp.${variant.activityName}"
			val componentName = ComponentName(context.packageName, activityName)
			try {
				val info = context.packageManager.getActivityInfo(
					componentName,
					PackageManager.GET_META_DATA or PackageManager.MATCH_DISABLED_COMPONENTS
				)
				val activityResources = context.packageManager.getResourcesForActivity(componentName)
				ResourcesCompat.getDrawable(activityResources, info.icon, null)
			} catch (_: Exception) {
				null
			}
		} ?: context.packageManager.getApplicationIcon(context.packageName)

		return drawable.toBitmap(512, 512).asImageBitmap()
	}
}

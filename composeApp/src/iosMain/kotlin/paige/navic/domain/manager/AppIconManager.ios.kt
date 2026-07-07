package paige.navic.domain.manager

import paige.navic.domain.models.settings.AppIconVariant

actual class AppIconManager {
	actual fun setVariant(newVariant: AppIconVariant) {}
	actual fun getIcon(variant: AppIconVariant): Any? = null
}

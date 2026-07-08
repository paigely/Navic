package paige.navic.domain.manager

actual class PermissionManager {
	actual fun openPermissionsSettings() {
		//UIApplication.sharedApplication.openURL(NSURL(string = UIApplicationOpenSettingsURLString))
	}
	actual suspend fun requestLocalNetworkPermission(): Boolean {
		// it's annoying to trigger this dialog on iOS
		return true
	}
}

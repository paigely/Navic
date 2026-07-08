package paige.navic.domain.manager

expect class PermissionManager {
	fun openPermissionsSettings()
	suspend fun requestLocalNetworkPermission(): Boolean
}

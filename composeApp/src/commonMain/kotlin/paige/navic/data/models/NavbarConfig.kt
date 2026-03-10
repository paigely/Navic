package paige.navic.data.models

import kotlinx.serialization.Serializable

@Serializable
data class NavbarConfig(
	val tabs: List<NavbarTab>,
	val version: Int
) {
	companion object {
		const val KEY = "navbarConfig"
		const val VERSION = 3
		val default = NavbarConfig(
			tabs = listOf(
				NavbarTab(NavbarTab.Id.LIBRARY, true),
				NavbarTab(NavbarTab.Id.ALBUMS, true),
				NavbarTab(NavbarTab.Id.PLAYLISTS, true),
				NavbarTab(NavbarTab.Id.ARTISTS, false),
				NavbarTab(NavbarTab.Id.SEARCH, false)
			),
			version = VERSION
		)
	}
}

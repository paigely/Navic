package paige.navic.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class NavTabId {
	LIBRARY,
	PLAYLISTS,
	ARTISTS
}

@Serializable
data class NavTabConfig(
	val id: NavTabId,
	val visible: Boolean
)


@Serializable
data class NavbarConfig(
	val tabs: List<NavTabConfig>,
	val version: Int
)

val defaultNavbarConfig = NavbarConfig(
	tabs = listOf(
		NavTabConfig(NavTabId.LIBRARY, true),
		NavTabConfig(NavTabId.PLAYLISTS, true),
		NavTabConfig(NavTabId.ARTISTS, false)
	),
	version = NAVBAR_CONFIG_VERSION
)

const val NAVBAR_CONFIG_KEY = "navbarConfig"
const val NAVBAR_CONFIG_VERSION = 1

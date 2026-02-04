package paige.navic.data.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import paige.subsonic.api.model.ListType
import paige.subsonic.api.model.TrackCollection

sealed interface Screen : NavKey {

	// tabs
	@Serializable
	data class Library(
		val nested: Boolean = false
	) : NavKey
	@Serializable
	data class Playlists(
		val nested: Boolean = false
	) : NavKey
	@Serializable
	data class Artists(
		val nested: Boolean = false
	) : NavKey
	@Serializable
	data class Albums(
		val nested: Boolean = false,
		val listType: ListType = ListType.ALPHABETICAL_BY_ARTIST
	) : NavKey

	// misc
	@Serializable data object Player : NavKey
	@Serializable data object Lyrics : NavKey
	@Serializable data class Tracks(val partialCollection: TrackCollection) : NavKey
	@Serializable data object Search : NavKey
	@Serializable data class Artist(val artist: String) : NavKey

	// settings
	sealed interface Settings : Screen {
		@Serializable data object Root : Settings
		@Serializable data object Appearance : Settings
		@Serializable data object Behaviour : Settings
		@Serializable data object About : Settings
		@Serializable data object Acknowledgements : Settings
	}
}
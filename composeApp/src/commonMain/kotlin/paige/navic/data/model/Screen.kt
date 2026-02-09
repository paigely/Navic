package paige.navic.data.model

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import paige.subsonic.api.model.ListType
import paige.subsonic.api.model.Track
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
		val listType: ListType? = null
	) : NavKey

	// misc
	@Serializable data object Player : NavKey
	@Serializable data object Lyrics : NavKey
	@Serializable data class Tracks(val partialCollection: TrackCollection) : NavKey
	@Serializable data class TrackInfo(val track: Track) : NavKey
	@Serializable data object Search : NavKey
	@Serializable data class Artist(val artist: String) : NavKey
	@Serializable data class AddToPlaylist(val tracks: List<Track>, val playlistToExclude: String? = null) : NavKey
	@Serializable data class CreatePlaylist(val tracks: List<Track> = emptyList()) : NavKey

	// settings
	sealed interface Settings : Screen {
		@Serializable data object Root : Settings
		@Serializable data object Appearance : Settings
		@Serializable data object Behaviour : Settings
		@Serializable data object About : Settings
		@Serializable data object Acknowledgements : Settings
	}
}
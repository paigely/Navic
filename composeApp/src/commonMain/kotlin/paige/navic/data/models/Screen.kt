package paige.navic.data.models

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import paige.subsonic.api.models.ListType
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection

@Serializable
sealed interface Screen : NavKey {

	// tabs
	@Serializable
	data class Library(
		val nested: Boolean = false
	) : Screen
	@Serializable
	data class Playlists(
		val nested: Boolean = false
	) : Screen
	@Serializable
	data class Artists(
		val nested: Boolean = false
	) : Screen
	@Serializable
	data class Albums(
		val nested: Boolean = false,
		val listType: ListType? = null
	) : Screen

	// misc
	@Serializable data object Player : Screen
	@Serializable data object Lyrics : Screen
	@Serializable data class Tracks(val partialCollection: TrackCollection) : Screen
	@Serializable data class TrackInfo(val track: Track) : Screen
	@Serializable data object Search : Screen
	@Serializable data object Shares : Screen
	@Serializable data class Artist(val artist: String) : Screen
	@Serializable data class AddToPlaylist(val tracks: List<Track>, val playlistToExclude: String? = null) : Screen
	@Serializable data class CreatePlaylist(val tracks: List<Track> = emptyList()) : Screen

	// settings
	@Serializable
	sealed interface Settings : Screen {
		@Serializable data object Root : Settings
		@Serializable data object Appearance : Settings
		@Serializable data object Behaviour : Settings
		@Serializable data object BottomAppBar : Settings
		@Serializable data object NowPlaying : Settings
		@Serializable data object Scrobbling : Settings
		@Serializable data object About : Settings
		@Serializable data object Acknowledgements : Settings
	}
}
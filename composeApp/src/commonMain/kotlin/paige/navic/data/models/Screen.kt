package paige.navic.data.models

import androidx.navigation3.runtime.NavKey
import dev.zt64.subsonic.api.model.AlbumListType
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.api.model.SongCollection
import kotlinx.serialization.Serializable

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
		val listType: AlbumListType? = null
	) : Screen
	@Serializable
	data class Genres(
		val nested: Boolean = false
	) : Screen

	// misc
	@Serializable data object NowPlaying : Screen
	@Serializable data object Lyrics : Screen
	@Serializable data object Queue : Screen
	@Serializable data class Tracks(val partialCollection: SongCollection, val tab: String) : Screen
	@Serializable data class TrackInfo(val track: Song) : Screen
	@Serializable data class Search(
		val nested: Boolean = false
	) : Screen
	@Serializable data object Shares : Screen
	@Serializable data class Artist(val artist: String) : Screen
	@Serializable data class AddToPlaylist(val tracks: List<Song>, val playlistToExclude: String? = null) : Screen
	@Serializable data class CreatePlaylist(val tracks: List<Song> = emptyList()) : Screen

	// settings
	@Serializable
	sealed interface Settings : Screen {
		@Serializable data object Root : Settings
		@Serializable data object Appearance : Settings
		@Serializable data object Playback : Settings
		@Serializable data object Developer : Settings
		@Serializable data object BottomAppBar : Settings
		@Serializable data object NowPlaying : Settings
		@Serializable data object About : Settings
		@Serializable data object Acknowledgements : Settings
		@Serializable data object Fonts : Settings
	}
}
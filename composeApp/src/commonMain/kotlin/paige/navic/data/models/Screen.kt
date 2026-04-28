package paige.navic.data.models

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import paige.navic.domain.models.DomainAlbumListType

@Immutable
@Serializable
sealed interface Screen : NavKey {

	// tabs
	@Immutable
	@Serializable
	data class Library(
		val nested: Boolean = false
	) : Screen

	@Immutable
	@Serializable
	data class PlaylistList(
		val nested: Boolean = false
	) : Screen

	@Immutable
	@Serializable
	data class ArtistList(
		val nested: Boolean = false
	) : Screen

	@Immutable
	@Serializable
	data class AlbumList(
		val nested: Boolean = false,
		val listType: DomainAlbumListType = DomainAlbumListType.AlphabeticalByArtist
	) : Screen

	@Immutable
	@Serializable
	data class GenreList(
		val nested: Boolean = false
	) : Screen

	@Immutable
	@Serializable
	data class SongList(
		val nested: Boolean = false,
		val artistId: String? = null,
		val artistName: String? = null
	) : Screen
	@Immutable
	@Serializable
	data class RadioList(
		val nested: Boolean = false
	) : Screen

	// misc
	@Immutable
	@Serializable
	data object Login : Screen
	@Immutable
	@Serializable
	data object NowPlaying : Screen
	@Immutable
	@Serializable
	data object Lyrics : Screen
	@Immutable
	@Serializable
	data object Queue : Screen
	@Immutable
	@Serializable
	data class CollectionDetail(
		val collectionId: String,
		val tab: String
	) : Screen

	@Immutable
	@Serializable
	data class SongDetail(val songId: String) : Screen
	@Immutable
	@Serializable
	data class Search(
		val nested: Boolean = false
	) : Screen

	@Immutable
	@Serializable
	data object ShareList : Screen
	@Immutable
	@Serializable
	data class ArtistDetail(val artist: String) : Screen

	// settings
	@Immutable
	@Serializable
	sealed interface Settings : Screen {
		@Immutable
		@Serializable
		data object Root : Settings
		@Immutable
		@Serializable
		data object Appearance : Settings
		@Immutable
		@Serializable
		data object Playback : Settings
		@Immutable
		@Serializable
		data object Developer : Settings
		@Immutable
		@Serializable
		data object BottomAppBar : Settings
		@Immutable
		@Serializable
		data object NowPlaying : Settings
		@Immutable
		@Serializable
		data object About : Settings
		@Immutable
		@Serializable
		data object Acknowledgements : Settings
		@Immutable
		@Serializable
		data object DataStorage : Settings
		@Immutable
		@Serializable
		data object Fonts : Settings
		@Immutable
		@Serializable
		data object CustomHeaders : Settings
		@Immutable
		@Serializable
		data object StreamingQuality : Settings
	}
}

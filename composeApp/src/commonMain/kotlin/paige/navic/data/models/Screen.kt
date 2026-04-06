package paige.navic.data.models

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection

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

	// misc
	@Immutable @Serializable data object NowPlaying : Screen
	@Immutable @Serializable data object Lyrics : Screen
	@Immutable @Serializable data object Queue : Screen
	@Immutable @Serializable data class TrackList(val partialCollection: DomainSongCollection, val tab: String) : Screen
	@Immutable @Serializable data class TrackDetail(val track: DomainSong) : Screen
	@Immutable @Serializable data class Search(
		val nested: Boolean = false
	) : Screen
	@Immutable @Serializable data object ShareList : Screen
	@Immutable @Serializable data class ArtistDetail(val artist: String) : Screen

	// settings
	@Immutable
	@Serializable
	sealed interface Settings : Screen {
		@Immutable @Serializable data object Root : Settings
		@Immutable @Serializable data object Appearance : Settings
		@Immutable @Serializable data object Playback : Settings
		@Immutable @Serializable data object Developer : Settings
		@Immutable @Serializable data object BottomAppBar : Settings
		@Immutable @Serializable data object NowPlaying : Settings
		@Immutable @Serializable data object About : Settings
		@Immutable @Serializable data object Acknowledgements : Settings
		@Immutable @Serializable data object DataStorage : Settings
		@Immutable @Serializable data object Fonts : Settings
	}
}
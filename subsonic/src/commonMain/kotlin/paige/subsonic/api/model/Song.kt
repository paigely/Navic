package paige.subsonic.api.model

import kotlinx.serialization.Serializable

@Serializable
data class SongResponse(
	val song: Song
)

@Serializable
data class SimilarSongsResponse(
	val similarSongs: SimilarSongs
)

@Serializable
data class SimilarSongs(
	val song: List<Song>
)

@Serializable
data class TopSongsResponse(
	val topSongs: TopSongs
)

@Serializable
data class TopSongs(
	val song: List<Song>
)

@Serializable
data class RandomSongsResponse(
	val randomSongs: RandomSongs
)

@Serializable
data class RandomSongs(
	val song: List<Song>
)

@Serializable
data class SongsByGenreResponse(
	val songsByGenre: SongsByGenre
)

@Serializable
data class SongsByGenre(
	val song: List<Song>
)

@Serializable
data class Song(
	val album: String?,
	val albumId: String?,
	val artist: String?,
	val artistId: String?,
	val bitRate: Int?,
	val contentType: String,
	val coverArt: String?,
	val created: String?,
	val duration: Int?,
	val genre: String?,
	val id: String,
	val isDir: Boolean?,
	val isVideo: Boolean?,
	val parent: String?,
	val path: String?,
	val playCount: Int?,
	val size: Int,
	val suffix: String,
	val title: String,
	val track: Int?,
	val type: String?,
	val year: Int?,
	val starred: String?,
	val averageRating: Double?,
	val userRating: Int?,
)

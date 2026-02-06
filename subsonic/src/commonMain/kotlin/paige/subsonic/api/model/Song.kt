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
	val song: List<Song>?
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
	override val album: String?,
	override val albumId: String?,
	override val artist: String?,
	override val artistId: String?,
	override val bitRate: Int?,
	override val contentType: String,
	override val coverArt: String?,
	override val created: String?,
	override val duration: Int?,
	override val genre: String?,
	override val id: String,
	override val isDir: Boolean?,
	override val isVideo: Boolean?,
	override val parent: String?,
	override val path: String?,
	override val playCount: Int?,
	override val size: Int,
	override val suffix: String,
	override val title: String,
	override val track: Int?,
	override val type: String?,
	override val year: Int?,
	override val starred: String?,
	val averageRating: Double?,
	override val userRating: Int?,
	override val bitDepth: Int?,
	override val channelCount: Int?,
	override val discNumber: Int?,
	override val samplingRate: Int?,
) : Track

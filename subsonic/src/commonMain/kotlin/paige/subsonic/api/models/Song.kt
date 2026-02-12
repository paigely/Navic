package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class SongResponse(
	val song: Track
)

@Serializable
data class SimilarSongsResponse(
	val similarSongs: SimilarSongs
)

@Serializable
data class SimilarSongs(
	val song: List<Track>
)

@Serializable
data class TopSongsResponse(
	val topSongs: TopSongs
)

@Serializable
data class TopSongs(
	val song: List<Track>?
)

@Serializable
data class RandomSongsResponse(
	val randomSongs: RandomSongs
)

@Serializable
data class RandomSongs(
	val song: List<Track>
)

@Serializable
data class SongsByGenreResponse(
	val songsByGenre: SongsByGenre
)

@Serializable
data class SongsByGenre(
	val song: List<Track>
)
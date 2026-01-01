package paige.subsonic.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ArtistsResponse(
	val artists: Artists
)

@Serializable
data class Artists(
	val ignoredArticles: String,
	val index: List<Index>,
) {

	@Serializable
	data class Index(
		val name: String,
		val artist: List<Artist>,
	)
}

@Serializable
data class ArtistResponse(
	val artist: Artist
)

@Serializable
data class Artist(
	val id: String,
	val name: String,
	val coverArt: String?,
	val albumCount: Int?,
	val artistImageUrl: String?,
	val album: List<Album>?,
	val userRating: Int?
)

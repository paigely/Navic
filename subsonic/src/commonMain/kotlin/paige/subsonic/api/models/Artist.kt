package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class ArtistsResponse(
	val artists: Artists
)

@Serializable
data class ArtistInfoResponse(
	val artistInfo: ArtistInfo
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

@Serializable
data class ArtistInfo(
	val biography: String?,
	val musicBrainzId: String?,
	val lastFmUrl: String?,
	val smallImageUrl: String?,
	val mediumImageUrl: String?,
	val largeImageUrl: String?,
	val similarArtist: List<Artist>?
)

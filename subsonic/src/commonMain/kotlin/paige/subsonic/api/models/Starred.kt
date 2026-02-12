package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class StarredResponse(
	val starred: StarredResult
)

@Serializable
data class Starred2Response(
	val starred2: StarredResult
)

@Serializable
data class StarredResult(
	val album: List<Album>?,
	val artist: List<Artist>?,
	val song: List<Track>?,
)

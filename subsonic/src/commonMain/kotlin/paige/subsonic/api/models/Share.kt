package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class CreateShareResponse(
	val shares: Map<String, List<Share>>,
)

@Serializable
data class SharesResponse(
	val shares: SharesData
) {
	@Serializable
	data class SharesData(
		val share: List<Share>?
	)
}

@Serializable
data class Share(
	val id: String,
	val url: String,
	val description: String?,
	val username: String,
	val created: String,
	val expires: String?,
	val lastVisited: String?,
	val visitCount: Int,
	val entry: List<Track>?
)

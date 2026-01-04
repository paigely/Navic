package paige.subsonic.api.model

import kotlinx.serialization.Serializable

@Serializable
class CreateShareResponse(
	val shares: Map<String, List<Share>>,
)

@Serializable
class Share(
	val id: String,
	val url: String,
)

package paige.subsonic.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubsonicResponse<T>(
	@SerialName("subsonic-response")
	val data: T
)

@Serializable
data class EmptyResponse(
	val status: String,
	val version: String
)

@Serializable
internal data class ErrorResponse(
	val status: String,
	val version: String,
	val error: Error,
)

@Serializable
internal data class Error(
	val code: Int,
	val message: String,
)

package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
	val user: User,
)

@Serializable
data class User(
	val username: String,
)

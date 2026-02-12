package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayQueueResponse(
	val playQueue: PlayQueue,
)

@Serializable
data class PlayQueue(
	val current: String?,
	val position: Long?,
	val username: String,
	val changed: String,
	val changedBy: String,
	val entry: List<Track>
)

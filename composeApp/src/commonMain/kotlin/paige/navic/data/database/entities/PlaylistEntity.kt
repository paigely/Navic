package paige.navic.data.database.entities

import androidx.room3.Entity
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
@Entity(primaryKeys = ["serverId", "playlistId"])
data class PlaylistEntity(
	val serverId: String,
	val playlistId: String,
	val name: String,
	val comment: String?,
	val owner: String,
	val coverArtId: String?,
	val songCount: Int,
	val duration: Duration,
	val public: Boolean?,
	val readOnly: Boolean?,
	val createdAt: Instant,
	val modifiedAt: Instant,
	val validUntil: Instant?,
	val allowedUsers: List<String>
)

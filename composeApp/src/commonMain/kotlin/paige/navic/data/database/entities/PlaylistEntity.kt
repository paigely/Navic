package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
@Entity
data class PlaylistEntity(
	@PrimaryKey val playlistId: String,
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

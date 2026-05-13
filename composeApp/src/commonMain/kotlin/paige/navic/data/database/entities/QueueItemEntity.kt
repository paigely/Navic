package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(
	tableName = "QueueItem",
)
data class QueueItemEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val serverId: String,
	val position: Int,
	val songJson: String
)

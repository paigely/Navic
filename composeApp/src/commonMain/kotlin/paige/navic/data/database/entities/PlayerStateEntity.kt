package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "PlayerState")
data class PlayerStateEntity(
	@PrimaryKey val serverId: String,
	val currentSongId: String?,
	val currentCollectionId: String?,
	val currentIndex: Int,
	val isShuffleEnabled: Boolean,
	val repeatMode: Int,
	val progress: Float,
	val playbackSpeed: Float
)

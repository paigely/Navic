package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class SyncActionType {
	STAR, UNSTAR, DELETE_PLAYLIST
}

@Entity
data class SyncActionEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val actionType: SyncActionType,
	val itemId: String
)
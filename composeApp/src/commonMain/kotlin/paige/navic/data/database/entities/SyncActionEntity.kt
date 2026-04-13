package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

enum class SyncActionType {
	STAR, UNSTAR, DELETE_PLAYLIST, SCROBBLE
}

@Entity
data class SyncActionEntity(
	@PrimaryKey(autoGenerate = true) val id: Int = 0,
	val actionType: SyncActionType,
	val itemId: String
)

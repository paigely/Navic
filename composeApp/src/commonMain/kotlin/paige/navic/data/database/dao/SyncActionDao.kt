package paige.navic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import paige.navic.data.database.entities.SyncActionEntity

@Dao
interface SyncActionDao {
	@Insert
	suspend fun enqueue(action: SyncActionEntity)

	@Transaction
	@Query("SELECT * FROM SyncActionEntity ORDER BY id ASC")
	suspend fun getPendingActions(): List<SyncActionEntity>

	@Query("DELETE FROM SyncActionEntity WHERE id = :id")
	suspend fun removeAction(id: Int)

	@Query("DELETE FROM SyncActionEntity")
	suspend fun clearAllActions()
}

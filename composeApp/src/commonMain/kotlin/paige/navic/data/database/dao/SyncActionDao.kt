package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.data.database.entities.SyncActionEntity

@Dao
interface SyncActionDao {
	@Insert
	suspend fun enqueue(action: SyncActionEntity)

	@Transaction
	@Query("SELECT * FROM SyncActionEntity WHERE serverId = :serverId ORDER BY id ASC")
	suspend fun getPendingActions(serverId: String): List<SyncActionEntity>

	@Query("DELETE FROM SyncActionEntity WHERE id = :id AND serverId = :serverId")
	suspend fun removeAction(id: Int, serverId: String)

	@Query("DELETE FROM SyncActionEntity WHERE serverId = :serverId")
	suspend fun clearActionsForServer(serverId: String)
}

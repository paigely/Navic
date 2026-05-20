package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.data.database.entities.PlayerStateEntity
import paige.navic.data.database.entities.QueueItemEntity

@Dao
interface PlayerStateDao {
	@Query("SELECT * FROM PlayerState WHERE serverId = :serverId")
	suspend fun getPlayerState(serverId: String): PlayerStateEntity?

	@Query("SELECT * FROM QueueItem WHERE serverId = :serverId ORDER BY position ASC")
	suspend fun getQueue(serverId: String): List<QueueItemEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertState(state: PlayerStateEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertQueue(items: List<QueueItemEntity>)

	@Query("DELETE FROM QueueItem WHERE serverId = :serverId")
	suspend fun clearQueue(serverId: String)

	@Transaction
	suspend fun saveFullState(serverId: String, state: PlayerStateEntity, queue: List<QueueItemEntity>) {
		insertState(state)
		clearQueue(serverId)
		insertQueue(queue)
	}
}

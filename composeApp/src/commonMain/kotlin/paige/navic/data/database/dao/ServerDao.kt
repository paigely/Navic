package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.ServerEntity

@Dao
interface ServerDao {
	@Query("SELECT * FROM ServerEntity ORDER BY lastUsedAt DESC")
	fun getAllServersFlow(): Flow<List<ServerEntity>>

	@Query("SELECT * FROM ServerEntity WHERE serverId = :id LIMIT 1")
	suspend fun getServerById(id: String): ServerEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertServer(server: ServerEntity)

	@Query("DELETE FROM ServerEntity WHERE serverId = :id")
	suspend fun deleteServer(id: String)

	@Query("UPDATE ServerEntity SET lastUsedAt = :timestamp WHERE serverId = :id")
	suspend fun updateLastUsed(id: String, timestamp: Long)
}

package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.RadioEntity

@Dao
interface RadioDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRadio(radio: RadioEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRadios(radios: List<RadioEntity>)

	@Query("SELECT * FROM RadioEntity WHERE serverId = :serverId ORDER BY name ASC")
	suspend fun getRadios(serverId: String): List<RadioEntity>

	@Query("SELECT * FROM RadioEntity WHERE serverId = :serverId ORDER BY name ASC")
	fun getRadiosFlow(serverId: String): Flow<List<RadioEntity>>

	@Query("DELETE FROM RadioEntity WHERE radioId = :radioId AND serverId = :serverId")
	suspend fun deleteRadio(radioId: String, serverId: String)

	@Query("DELETE FROM RadioEntity WHERE serverId = :serverId")
	suspend fun clearRadiosForServer(serverId: String)

	@Query("SELECT radioId FROM RadioEntity WHERE serverId = :serverId")
	suspend fun getAllRadioIds(serverId: String): List<String>

	@Query("DELETE FROM RadioEntity WHERE serverId = :serverId AND radioId IN (:ids)")
	suspend fun deleteRadios(serverId: String, ids: List<String>)

	@Transaction
	suspend fun deleteObsoleteRadios(serverId: String, remoteIds: Set<String>) {
		val localIds = getAllRadioIds(serverId)
		val toDelete = localIds.filter { it !in remoteIds }
		if (toDelete.isNotEmpty()) {
			toDelete.chunked(900).forEach { chunk ->
				deleteRadios(serverId, chunk)
			}
		}
	}

	@Transaction
	suspend fun updateAllRadios(serverId: String, remoteRadios: List<RadioEntity>) {
		val remoteIds = remoteRadios.map { it.radioId }.toSet()
		deleteObsoleteRadios(serverId, remoteIds)
		insertRadios(remoteRadios)
	}
}

package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.RadioEntity
import paige.navic.shared.Logger

@Dao
interface RadioDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRadio(radio: RadioEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertRadios(radios: List<RadioEntity>)

	@Query("SELECT * FROM RadioEntity ORDER BY name ASC")
	suspend fun getRadios(): List<RadioEntity>

	@Query("SELECT * FROM RadioEntity ORDER BY name ASC")
	fun getRadiosFlow(): Flow<List<RadioEntity>>

	@Query("DELETE FROM RadioEntity WHERE radioId = :radioId")
	suspend fun deleteRadio(radioId: String)

	@Query("DELETE FROM RadioEntity")
	suspend fun clearAllRadios()

	@Query("SELECT radioId FROM RadioEntity")
	suspend fun getAllRadioIds(): List<String>

	@Transaction
	suspend fun updateAllRadios(remoteRadios: List<RadioEntity>) {
		val remoteIds = remoteRadios.map { it.radioId }.toSet()
		getAllRadioIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("RadioDao", "Radio $localId no longer exists remotely")
				deleteRadio(localId)
			}
		}
		insertRadios(remoteRadios)
	}
}

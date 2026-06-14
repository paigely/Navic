package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import paige.navic.data.database.entities.CoverColorEntity

@Dao
interface CoverColorDao {
	@Query("SELECT * FROM cover_colors WHERE coverArtId = :coverArtId")
	suspend fun getColor(coverArtId: String): CoverColorEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertColor(color: CoverColorEntity)
}

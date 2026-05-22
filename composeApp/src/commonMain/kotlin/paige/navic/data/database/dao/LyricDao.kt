package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.data.database.entities.LyricEntity

@Dao
interface LyricDao {
	@Transaction
	@Query("SELECT * FROM LyricEntity WHERE songId = :songId")
	suspend fun getLyrics(songId: String): LyricEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertLyrics(lyrics: LyricEntity)

	@Query("DELETE FROM LyricEntity")
	suspend fun clearAllLyrics()
}

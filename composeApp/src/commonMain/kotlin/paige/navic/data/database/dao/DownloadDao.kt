package paige.navic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus

@Dao
interface DownloadDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertDownload(download: DownloadEntity)

	@Query("SELECT * FROM DownloadEntity WHERE songId = :songId")
	suspend fun getDownloadById(songId: String): DownloadEntity?

	@Query("SELECT * FROM DownloadEntity")
	fun getAllDownloads(): Flow<List<DownloadEntity>>

	@Query("SELECT COUNT(*) FROM DownloadEntity")
	fun getDownloadsCount(): Flow<Int>

	@Query("DELETE FROM DownloadEntity WHERE songId = :songId")
	suspend fun deleteDownload(songId: String)

	@Query("UPDATE DownloadEntity SET status = :status, progress = :progress WHERE songId = :songId")
	suspend fun updateProgress(songId: String, status: DownloadStatus, progress: Float)

	@Query("DELETE FROM DownloadEntity")
	suspend fun clearAllDownloads()
}

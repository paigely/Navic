package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus

@Dao
interface DownloadDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertDownload(download: DownloadEntity)

	@Query("SELECT * FROM DownloadEntity WHERE songId = :songId AND serverId = :serverId")
	suspend fun getDownloadById(songId: String, serverId: String): DownloadEntity?

	@Query("SELECT * FROM DownloadEntity WHERE serverId = :serverId")
	fun getAllDownloads(serverId: String): Flow<List<DownloadEntity>>

	@Query("SELECT * FROM DownloadEntity WHERE serverId = :serverId")
	suspend fun getAllDownloadsList(serverId: String): List<DownloadEntity>

	@Query("SELECT COUNT(*) FROM DownloadEntity WHERE status = :status AND serverId = :serverId")
	fun getDownloadsCount(serverId: String, status: DownloadStatus = DownloadStatus.DOWNLOADED): Flow<Int>

	@Query("DELETE FROM DownloadEntity WHERE songId = :songId AND serverId = :serverId")
	suspend fun deleteDownload(songId: String, serverId: String)

	@Query("UPDATE DownloadEntity SET status = :status, progress = :progress WHERE songId = :songId AND serverId = :serverId")
	suspend fun updateProgress(songId: String, serverId: String, status: DownloadStatus, progress: Float)

	@Query("DELETE FROM DownloadEntity WHERE serverId = :serverId")
	suspend fun clearDownloadsForServer(serverId: String)
}

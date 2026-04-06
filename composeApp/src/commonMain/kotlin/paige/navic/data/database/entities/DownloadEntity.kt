package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity
data class DownloadEntity(
	@PrimaryKey val songId: String,
	val status: DownloadStatus,
	val progress: Float = 0f,
	val filePath: String? = null
)

@Serializable
enum class DownloadStatus {
	NOT_DOWNLOADED,
	DOWNLOADING,
	DOWNLOADED,
	FAILED
}

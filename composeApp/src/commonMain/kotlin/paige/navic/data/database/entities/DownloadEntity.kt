package paige.navic.data.database.entities

import androidx.room3.Entity
import kotlinx.serialization.Serializable

@Serializable
@Entity(primaryKeys = ["serverId", "songId"])
data class DownloadEntity(
	val serverId: String,
	val songId: String,
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

package paige.navic.data.database.entities

import androidx.room3.Entity
import paige.navic.domain.repositories.LyricsProvider

@Entity(primaryKeys = ["serverId", "songId"])
data class LyricEntity(
	val serverId: String,
	val songId: String,
	val rawContent: String,
	val provider: LyricsProvider
)

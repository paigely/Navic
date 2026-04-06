package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import paige.navic.domain.repositories.LyricsProvider

@Entity
data class LyricEntity(
	@PrimaryKey val songId: String,
	val rawContent: String,
	val provider: LyricsProvider
)
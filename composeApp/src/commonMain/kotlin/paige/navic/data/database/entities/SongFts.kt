package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.Fts4

@Fts4(contentEntity = SongEntity::class)
@Entity
data class SongFts(
	val title: String,
	val artistName: String,
	val albumTitle: String?
)

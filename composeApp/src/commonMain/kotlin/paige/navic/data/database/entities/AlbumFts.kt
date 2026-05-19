package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.Fts4

@Fts4(contentEntity = AlbumEntity::class)
@Entity
data class AlbumFts(
	val name: String,
	val artistName: String
)

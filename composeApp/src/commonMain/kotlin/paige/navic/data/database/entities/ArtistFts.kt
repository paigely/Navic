package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.Fts4

@Fts4(contentEntity = ArtistEntity::class)
@Entity
data class ArtistFts(
	val name: String
)

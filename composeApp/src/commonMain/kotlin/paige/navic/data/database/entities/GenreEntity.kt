package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class GenreEntity(
	@PrimaryKey val genreName: String,
	val albumCount: Int,
	val songCount: Int
)

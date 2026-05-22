package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class GenreEntity(
	@PrimaryKey val genreName: String,
	val albumCount: Int,
	val songCount: Int
)

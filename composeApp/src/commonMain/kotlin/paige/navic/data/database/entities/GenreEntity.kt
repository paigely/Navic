package paige.navic.data.database.entities

import androidx.room3.Entity

@Entity(primaryKeys = ["serverId", "genreName"])
data class GenreEntity(
	val serverId: String,
	val genreName: String,
	val albumCount: Int,
	val songCount: Int
)

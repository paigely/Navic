package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class LyricEntity(
	@PrimaryKey val songId: String,
	val rawContent: String,
	val providerName: String
)

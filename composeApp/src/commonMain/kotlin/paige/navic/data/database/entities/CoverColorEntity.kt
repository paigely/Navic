package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "cover_colors")
data class CoverColorEntity(
	@PrimaryKey val coverArtId: String,
	val color: Int
)

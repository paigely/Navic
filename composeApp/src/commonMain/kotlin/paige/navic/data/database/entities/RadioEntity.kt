package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RadioEntity (
	@PrimaryKey val radioId: String,
	val name: String,
	val streamUrl: String,
	val homepageUrl: String? = null
)

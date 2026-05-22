package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class RadioEntity (
	@PrimaryKey val radioId: String,
	val name: String,
	val streamUrl: String,
	val homepageUrl: String? = null
)

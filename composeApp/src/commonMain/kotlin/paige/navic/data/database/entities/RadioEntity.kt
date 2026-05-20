package paige.navic.data.database.entities

import androidx.room3.Entity

@Entity(primaryKeys = ["serverId", "radioId"])
data class RadioEntity (
	val serverId: String,
	val radioId: String,
	val name: String,
	val streamUrl: String,
	val homepageUrl: String? = null
)

package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.Index
import kotlin.time.Instant

@Entity(
	indices = [
		Index(value = ["name"])
	],
	primaryKeys = ["serverId", "artistId"]
)
data class ArtistEntity(
	val serverId: String,
	val artistId: String,
	val name: String,
	val albumCount: Int = 0,
	val coverArtId: String? = null,
	val artistImageUrl: String? = null,
	val starredAt: Instant? = null,
	val userRating: Int? = null,
	val sortName: String? = null,
	val musicBrainzId: String? = null,
	val lastFmUrl: String? = null,
	val roles: List<String> = emptyList(),
	val biography: String? = null,
	val similarArtistIds: List<String> = emptyList()
)

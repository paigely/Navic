package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity
data class ArtistEntity (
	@PrimaryKey val artistId: String,
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
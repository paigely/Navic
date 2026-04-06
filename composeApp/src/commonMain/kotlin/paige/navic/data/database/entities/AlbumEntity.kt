package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Duration
import kotlin.time.Instant

@Entity
data class AlbumEntity(
	@PrimaryKey val albumId: String,
	val name: String,
	val artistName: String,
	val artistId: String,
	val year: Int?,
	val coverArtId: String,
	val genre: String?,
	val genres: List<String>,
	val songCount: Int,
	val duration: Duration?,
	val createdAt: Instant,
	val starredAt: Instant?,
	val lastPlayedAt: Instant?,
	val playCount: Int = 0,
	val userRating: Int?,
	val version: String?,
	val musicBrainzId: String?,
)

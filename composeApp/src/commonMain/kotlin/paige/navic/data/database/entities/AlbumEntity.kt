package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.Index
import kotlin.time.Duration
import kotlin.time.Instant

@Entity(
	indices = [
		Index(value = ["artistId"]),
		Index(value = ["name"]),
		Index(value = ["createdAt"]),
		Index(value = ["genre"])
	],
	primaryKeys = ["serverId", "albumId"]
)
data class AlbumEntity(
	val serverId: String,
	val albumId: String,
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

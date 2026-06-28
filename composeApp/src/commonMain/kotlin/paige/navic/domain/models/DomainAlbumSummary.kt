package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
@Serializable
data class DomainAlbumSummary(
	val id: String,
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
	val musicBrainzId: String?
)

package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Immutable
@Serializable
data class DomainArtist(
	val id: String,
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
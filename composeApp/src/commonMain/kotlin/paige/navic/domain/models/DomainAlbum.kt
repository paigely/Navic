package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
@Serializable
data class DomainAlbum(
	override val id: String,
	override val name: String,
	val artistName: String,
	val artistId: String,
	val year: Int?,
	override val coverArtId: String,
	val genre: String?,
	val genres: List<String>,
	override val songCount: Int,
	override val duration: Duration?,
	val createdAt: Instant,
	val starredAt: Instant?,
	val lastPlayedAt: Instant?,
	val playCount: Int = 0,
	val userRating: Int?,
	val version: String?,
	val musicBrainzId: String?,
	override val songs: List<DomainSong>
) : DomainSongCollection

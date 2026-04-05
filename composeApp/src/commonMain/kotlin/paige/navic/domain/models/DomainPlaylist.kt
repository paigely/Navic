package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
@Serializable
data class DomainPlaylist(
	override val id: String,
	override val name: String,
	val owner: String,
	val comment: String?,
	override val coverArtId: String?,
	override val songCount: Int,
	override val duration: Duration,
	val createdAt: Instant,
	val modifiedAt: Instant,
	val public: Boolean?,
	val readOnly: Boolean?,
	val allowedUsers: List<String>,
	val validUntil: Instant?,
	override val songs: List<DomainSong>
) : DomainSongCollection

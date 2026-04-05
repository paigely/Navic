package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration

@Immutable
@Serializable
sealed interface DomainSongCollection {
	val id: String
	val name: String
	val coverArtId: String?
	val duration: Duration?
	val songCount: Int
	val songs: List<DomainSong>
}


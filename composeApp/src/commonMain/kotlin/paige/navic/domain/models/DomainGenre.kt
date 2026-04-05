package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DomainGenre(
	val name: String,
	val albumCount: Int,
	val songCount: Int,
	val albums: List<DomainAlbum>
)

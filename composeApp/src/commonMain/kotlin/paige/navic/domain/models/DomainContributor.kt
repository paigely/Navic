package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DomainContributor(
	val role: String,
	val subRole: String?,
	val artistId: String,
	val artistName: String
)
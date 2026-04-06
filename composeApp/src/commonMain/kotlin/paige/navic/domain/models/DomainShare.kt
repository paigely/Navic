package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import dev.zt64.subsonic.api.model.SubsonicResource
import kotlin.time.Instant

@Immutable
data class DomainShare(
	val id: String,
	val url: String,
	val description: String,
	val username: String,
	val createdAt: Instant,
	val expiresAt: Instant,
	val lastVisited: Instant?,
	val visitCount: Int,
	val items: List<SubsonicResource>
)

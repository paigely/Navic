package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DomainRadio(
	val id: String,
	val name: String,
	val streamUrl: String,
	val homepageUrl: String?
)

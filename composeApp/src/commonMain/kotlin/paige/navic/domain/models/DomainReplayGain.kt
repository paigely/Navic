package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class DomainReplayGain(
	val albumGain: Float?,
	val albumPeak: Float?,
	val trackGain: Float?,
	val trackPeak: Float?,
	val baseGain: Float?,
	val fallbackGain: Float?
)
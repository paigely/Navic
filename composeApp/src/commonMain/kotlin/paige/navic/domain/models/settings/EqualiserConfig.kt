package paige.navic.domain.models.settings

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class EqualiserConfig(
	val bandLevels: Map<Int, Float> = emptyMap(),
	val bandCount: Int = 0,
	val bandLowerRange: Float = 0f,
	val bandUpperRange: Float = 0f,
	val mode: EqualiserMode = EqualiserMode.Disabled
)

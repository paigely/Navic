package paige.navic.utils

import dev.zt64.subsonic.api.model.Song.ReplayGain
import kotlin.math.pow

fun ReplayGain.effectiveGain(): Float {
	val gain = trackGain ?: albumGain ?: fallbackGain ?: baseGain ?: 0f
	return (10.0.pow((gain / 20.0)).toFloat()).coerceIn(0f..1f)
}

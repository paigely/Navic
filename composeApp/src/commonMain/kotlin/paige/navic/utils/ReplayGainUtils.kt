package paige.navic.utils

import paige.navic.domain.models.DomainReplayGain
import kotlin.math.pow

fun DomainReplayGain.effectiveGain(): Float {
	val gain = trackGain ?: albumGain ?: fallbackGain ?: baseGain ?: 0f
	return (10.0.pow((gain / 20.0)).toFloat()).coerceIn(0f..1f)
}
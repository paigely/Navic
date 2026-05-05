package paige.navic.utils

import paige.navic.data.models.settings.enums.ReplayGainMode
import paige.navic.domain.models.DomainReplayGain
import kotlin.math.pow

fun DomainReplayGain.effectiveGain(mode: ReplayGainMode = ReplayGainMode.Track): Float {
	val gain = if (mode == ReplayGainMode.Track) {
		trackGain ?: albumGain ?: fallbackGain ?: baseGain ?: 0f
	} else {
		albumGain ?: trackGain ?: fallbackGain ?: baseGain ?: 0f
	}
	return (10.0.pow((gain / 20.0)).toFloat()).coerceIn(0f..1f)
}

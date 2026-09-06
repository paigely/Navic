package paige.navic.util.core

import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
import kotlin.math.pow

fun DomainReplayGain.effectiveGain(mode: ReplayGainMode = ReplayGainMode.Track): Float {
	return if (mode == ReplayGainMode.Track) {
		trackGain ?: albumGain ?: fallbackGain ?: baseGain ?: 0f
	} else {
		albumGain ?: trackGain ?: fallbackGain ?: baseGain ?: 0f
	}
}

fun Float.decibelsToLinear(): Float {
	return 10.0f.pow(this / 20.0f)
}

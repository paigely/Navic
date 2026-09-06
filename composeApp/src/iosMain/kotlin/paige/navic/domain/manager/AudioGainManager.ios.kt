package paige.navic.domain.manager

import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode

actual class AudioGainManager {
	actual fun setAmplifierValues(withReplayGain: Float, withoutReplayGain: Float) {
	}

	actual fun setReplayGainMetadata(metadata: DomainReplayGain?) {
	}

	actual fun applyGainMode(mode: ReplayGainMode) {
	}

	actual fun resetGain() {
	}

}

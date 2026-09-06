package paige.navic.domain.manager

import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode

expect class AudioGainManager {
	fun setAmplifierValues(withReplayGain: Float, withoutReplayGain: Float)
	fun setReplayGainMetadata(metadata: DomainReplayGain?)
	fun applyGainMode(mode: ReplayGainMode)
	fun resetGain()
}

package paige.navic.exoplayer

import android.media.AudioFormat
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.AudioProcessor.StreamMetadata
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.settings.ReplayGainMode
import paige.navic.util.core.decibelsToLinear
import paige.navic.util.core.effectiveGain
import java.nio.ByteBuffer
import java.nio.ByteOrder


@UnstableApi
class AudioGainProcessor : BaseAudioProcessor() {
	private companion object {
		const val DEFAULT_GAIN = 1f
	}

	private var replayGainMetadata: DomainReplayGain? = null

	private val finalVolume: Float
		get() {
			// ugly hack: we are checking the volume here because the metadata may get returned anyway, but with its parameters values null
			return if (this.replayGainMetadata != null && this.volume.toDouble() != 0.0) {
				(volume + rgAmpValue).decibelsToLinear()
			} else {
				(volume + ampValue).decibelsToLinear()
			}
		}

	// we should ONLY flush if the gain has changed, flushing needlessly will cause some "bits" of the music to skip
	// flushing the stream is needed because otherwise the user might hear some crackling after changing values
	private var volume = DEFAULT_GAIN
		set(value) {
			if (field != value) flush(StreamMetadata.DEFAULT)
			field = value
		}

	var rgAmpValue = 0f
		set(value) {
			if (field != value) flush(StreamMetadata.DEFAULT)
			field = value
		}

	var ampValue = 0f
		set(value) {
			if (field != value) flush(StreamMetadata.DEFAULT)
			field = value
		}

	fun applyGainMode(mode: ReplayGainMode) {
		volume = replayGainMetadata?.effectiveGain(mode) ?: DEFAULT_GAIN
	}

	fun setReplayGainMetadata(metadata: DomainReplayGain?) {
		replayGainMetadata = metadata
	}

	fun resetGain() {
		volume = DEFAULT_GAIN
	}

	override fun isActive(): Boolean {
		return super.isActive() && finalVolume != DEFAULT_GAIN
	}

	override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
		if (inputAudioFormat.encoding == AudioFormat.ENCODING_PCM_16BIT) {
			return inputAudioFormat
		}
		throw AudioProcessor.UnhandledAudioFormatException(inputAudioFormat)
	}

	override fun queueInput(inputBuffer: ByteBuffer) {
		val pos = inputBuffer.position()
		val limit = inputBuffer.limit()
		val outputBuffer = replaceOutputBuffer(limit - pos)

		inputBuffer.order(ByteOrder.LITTLE_ENDIAN)
		outputBuffer.order(ByteOrder.LITTLE_ENDIAN)

		val computedVolume = finalVolume

		if (computedVolume == DEFAULT_GAIN) {
			outputBuffer.put(inputBuffer)
		} else {
			val shortBufferInput = inputBuffer.asShortBuffer()
			val shortBufferOutput = outputBuffer.asShortBuffer()

			while (shortBufferInput.hasRemaining()) {
				// prevent popping
				val sample = shortBufferInput.get()
				val scaledSample = (sample * computedVolume)
					.toInt()
					.coerceAtLeast(Short.MIN_VALUE.toInt())
					.coerceAtMost(Short.MAX_VALUE.toInt())
					.toShort()

				shortBufferOutput.put(scaledSample)
			}
			inputBuffer.position(inputBuffer.position() + shortBufferInput.position() * 2)
			outputBuffer.position(outputBuffer.position() + shortBufferOutput.position() * 2)
		}
		outputBuffer.flip()
	}
}

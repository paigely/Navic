package paige.navic.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import paige.navic.data.models.Settings
import paige.navic.data.session.SessionManager
import kotlin.time.Clock

interface ScrobblePlayerSource {
	val currentPosition: Long
	val duration: Long
}

class ScrobbleManager(
	private val playerSource: ScrobblePlayerSource,
	private val scope: CoroutineScope
) {
	private var currentMediaId: String? = null
	private var hasScrobbledCurrent = false
	private var progressJob: Job? = null
	private var accumulatedPlayTime: Long = 0

	fun onMediaChanged(mediaId: String?) {
		currentMediaId = mediaId
		hasScrobbledCurrent = false
		accumulatedPlayTime = 0

		progressJob?.cancel()

		scrobbleNowPlaying(mediaId)
	}

	fun onPlayStateChanged(isPlaying: Boolean) {
		if (isPlaying) {
			startProgressTracker()
		} else {
			progressJob?.cancel()
		}
	}

	private fun startProgressTracker() {
		progressJob?.cancel()
		progressJob = scope.launch(Dispatchers.Main) {
			var lastTickTime = Clock.System.now().toEpochMilliseconds()

			while (isActive) {
				val now = Clock.System.now().toEpochMilliseconds()
				val timePassed = now - lastTickTime
				lastTickTime = now

				accumulatedPlayTime += timePassed

				checkProgress()
				delay(2000)
			}
		}
	}

	private fun checkProgress() {
		if (hasScrobbledCurrent) return

		val duration = playerSource.duration
		if (duration <= 0) return

		val percent = accumulatedPlayTime.toFloat() / duration.toFloat()
		val playedEnoughPercent = percent >= Settings.shared.scrobblePercentage
		val isValidTrack = duration >= Settings.shared.minDurationToScrobble

		if (isValidTrack && playedEnoughPercent) {
			scrobbleSubmission(currentMediaId)
			hasScrobbledCurrent = true
		}
	}

	private fun scrobbleSubmission(trackId: String?) {
		if (!Settings.shared.enableScrobbling) return
		scope.launch(Dispatchers.IO) {
			try {
				trackId?.let {
					SessionManager.api.scrobble(
						it,
						Clock.System.now().toEpochMilliseconds(),
						submission = true
					)
				}
			} catch (_: Exception) {
			}
		}
	}

	private fun scrobbleNowPlaying(trackId: String?) {
		if (!Settings.shared.enableScrobbling) return
		scope.launch(Dispatchers.IO) {
			try {
				trackId?.let {
					SessionManager.api.scrobble(
						it,
						Clock.System.now().toEpochMilliseconds(),
						submission = false
					)
				}
			} catch (_: Exception) {
			}
		}
	}
}
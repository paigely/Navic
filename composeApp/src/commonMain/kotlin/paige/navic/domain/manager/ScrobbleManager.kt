package paige.navic.domain.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import paige.navic.data.database.entities.SyncActionType
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

interface ScrobblePlayerSource {
	val currentPosition: Long
	val duration: Long
	val isPlaying: Boolean
}

class ScrobbleManager(
	private val playerSource: ScrobblePlayerSource,
	private val connectivityManager: ConnectivityManager,
	private val syncManager: SyncManager,
	private val sessionManager: SessionManager,
	private val scope: CoroutineScope,
	private val preferenceManager: PreferenceManager
) {
	private var currentMediaId: String? = null
	private var hasScrobbledCurrent = false
	private var hasSentNowPlaying = false
	private var progressJob: Job? = null
	private var accumulatedPlayTime: Long = 0

	fun onMediaChanged(mediaId: String?) {
		currentMediaId = mediaId
		hasScrobbledCurrent = false
		accumulatedPlayTime = 0

		progressJob?.cancel()
		if (playerSource.isPlaying) {
			startProgressTracker()
			scrobbleNowPlaying(mediaId)
			hasSentNowPlaying = true
		}
	}

	fun onPlayStateChanged(isPlaying: Boolean) {
		if (isPlaying) {
			startProgressTracker()
			if (!hasSentNowPlaying) {
				scrobbleNowPlaying(currentMediaId)
				hasSentNowPlaying = true
			}
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
				delay(2.seconds)
			}
		}
	}

	private fun checkProgress() {
		if (hasScrobbledCurrent) return

		val duration = playerSource.duration
		if (duration <= 0) return

		val percent = accumulatedPlayTime.toFloat() / duration.toFloat()
		val playedEnoughPercent = percent >= preferenceManager.scrobblePercentage
		val isValidSong = duration >= preferenceManager.minDurationToScrobble

		if (isValidSong && playedEnoughPercent) {
			scrobbleSubmission(currentMediaId)
			hasScrobbledCurrent = true
		}
	}

	private fun scrobbleSubmission(songId: String?) {
		if (!preferenceManager.enableScrobbling || songId == null) return

		scope.launch(Dispatchers.IO) {
			if (connectivityManager.isOnline.value) {
				try {
					sessionManager.api.scrobble(songId, submission = true)
				} catch (_: Exception) {
					syncManager.enqueueAction(SyncActionType.SCROBBLE, songId)
				}
			} else {
				syncManager.enqueueAction(SyncActionType.SCROBBLE, songId)
			}
		}
	}

	private fun scrobbleNowPlaying(songId: String?) {
		if (!preferenceManager.enableScrobbling || songId == null) return

		if (!connectivityManager.isOnline.value) return

		scope.launch(Dispatchers.IO) {
			try {
				sessionManager.api.scrobble(songId, submission = false)
			} catch (_: Exception) {
			}
		}
	}
}

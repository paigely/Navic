package paige.navic.managers

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import paige.navic.shared.MediaPlayerViewModel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class SleepTimerManager(private val player: MediaPlayerViewModel, val scope: CoroutineScope) {
	private var job: Job? = null
	var endTimeStamp: Instant? = null
		private set

	val timeLeft: Duration?
		get() = endTimeStamp?.let{ it - Clock.System.now() }

	fun startTimer(duration: Duration) {
		job?.cancel()

		job = scope.launch {
			endTimeStamp = Clock.System.now() + duration

			delay(duration)
			player.pause()
			stopTimer()
		}
	}

	fun stopTimer() {
		job?.cancel()
		job = null
		endTimeStamp = null
	}
}

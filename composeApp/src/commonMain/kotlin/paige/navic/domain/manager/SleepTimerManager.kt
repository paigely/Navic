package paige.navic.domain.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import paige.navic.shared.MediaPlayerViewModel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

class SleepTimerManager(
	private val player: MediaPlayerViewModel
) {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
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

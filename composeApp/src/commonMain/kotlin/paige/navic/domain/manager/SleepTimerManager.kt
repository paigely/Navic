package paige.navic.domain.manager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import paige.navic.shared.MediaPlayerViewModel
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

sealed interface SleepTimerMode {
	data object Disabled : SleepTimerMode
	data class Time(val endTime: Instant) : SleepTimerMode
	data class Songs(val remaining: Int) : SleepTimerMode
	data object EndOfQueue : SleepTimerMode
}

class SleepTimerManager(
	private val player: MediaPlayerViewModel
) {
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
	private var job: Job? = null
	private var observerJob: Job? = null
	private var remainingSongs = 0

	private val _mode = MutableStateFlow<SleepTimerMode>(SleepTimerMode.Disabled)
	val mode = _mode.asStateFlow()

	val endTimeStamp: Instant?
		get() = (mode.value as? SleepTimerMode.Time)?.endTime

	val timeLeft: Duration?
		get() = endTimeStamp?.let { it - Clock.System.now() }

	fun startTimer(duration: Duration) {
		stopTimer()
		val endTime = Clock.System.now() + duration
		_mode.value = SleepTimerMode.Time(endTime)

		job = scope.launch {
			delay(duration)
			player.pause()
			stopTimer()
		}
	}

	fun startSongsTimer(count: Int) {
		stopTimer()
		remainingSongs = count
		_mode.value = SleepTimerMode.Songs(count)
		observeSongs()
	}

	fun startEndOfQueueTimer() {
		stopTimer()
		val state = player.uiState.value
		remainingSongs = if (state.currentIndex != -1) {
			state.queue.size - state.currentIndex
		} else {
			0
		}

		if (remainingSongs > 0) {
			_mode.value = SleepTimerMode.EndOfQueue
			observeSongs()
		}
	}

	private fun observeSongs() {
		observerJob?.cancel()
		observerJob = scope.launch {
			player.uiState
				.map { it.currentIndex }
				.distinctUntilChanged()
				.drop(1)
				.collect {
					remainingSongs--
					if (remainingSongs <= 0) {
						player.pause()
						stopTimer()
					} else {
						val currentMode = _mode.value
						if (currentMode is SleepTimerMode.Songs) {
							_mode.value = SleepTimerMode.Songs(remainingSongs)
						}
					}
				}
		}
	}

	fun stopTimer() {
		job?.cancel()
		job = null
		observerJob?.cancel()
		observerJob = null
		remainingSongs = 0
		_mode.value = SleepTimerMode.Disabled
	}
}

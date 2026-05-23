package paige.navic.domain.manager

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.currentItem
import platform.AVFoundation.currentTime
import platform.AVFoundation.duration
import platform.CoreMedia.CMTimeGetSeconds

class IOSScrobbleManager(
    private val player: AVPlayer,
    scope: CoroutineScope,
    connectivityManager: ConnectivityManager,
    syncManager: SyncManager,
    sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) {
	@OptIn(ExperimentalForeignApi::class)
	private val playerSource = object : ScrobblePlayerSource {
		override val currentPosition: Long
			get() = (CMTimeGetSeconds(player.currentTime()) * 1000).toLong()

		override val duration: Long
			get() {
				val duration = player.currentItem?.duration ?: return 0L
				val seconds = CMTimeGetSeconds(duration)
				return if (seconds.isNaN()) 0L else (seconds * 1000).toLong()
			}
	}


	private val scrobbleManager =
        ScrobbleManager(playerSource, connectivityManager, syncManager, sessionManager, scope, preferenceManager)

	fun onMediaChanged(mediaId: String?) {
		scrobbleManager.onMediaChanged(mediaId)
	}

	fun onIsPlayingChanged(isPlaying: Boolean) {
		scrobbleManager.onPlayStateChanged(isPlaying)
	}
}

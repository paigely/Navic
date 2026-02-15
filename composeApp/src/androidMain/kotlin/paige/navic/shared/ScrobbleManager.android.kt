package paige.navic.shared

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope

class AndroidScrobbleManager(
	private val player: Player,
	scope: CoroutineScope
) : Player.Listener {

	private val playerSource = object : ScrobblePlayerSource {
		override val currentPosition: Long get() = player.currentPosition
		override val duration: Long get() = player.duration
	}

	private val scrobbleManager = ScrobbleManager(playerSource, scope)

	init {
		player.addListener(this)
	}

	override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
		scrobbleManager.onMediaChanged(mediaItem?.mediaId)
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		scrobbleManager.onPlayStateChanged(isPlaying)
	}

	fun release() {
		player.removeListener(this)
	}
}
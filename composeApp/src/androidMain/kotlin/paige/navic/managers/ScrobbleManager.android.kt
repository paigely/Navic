package paige.navic.managers

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.CoroutineScope
import paige.navic.data.database.SyncManager

class AndroidScrobbleManager(
	private val player: Player,
	scope: CoroutineScope,
	connectivityManager: ConnectivityManager,
	syncManager: SyncManager
) : Player.Listener {

	private val playerSource = object : ScrobblePlayerSource {
		override val currentPosition: Long get() = player.currentPosition
		override val duration: Long get() = player.duration
	}

	private val scrobbleManager = ScrobbleManager(playerSource, connectivityManager, syncManager, scope)

	init {
		player.addListener(this)
	}

	override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
		scrobbleManager.onMediaChanged(mediaItem?.mediaId)
	}

	override fun onPositionDiscontinuity(
		oldPosition: Player.PositionInfo,
		newPosition: Player.PositionInfo,
		reason: Int
	) {
		val isSeekOrRepeat = reason == Player.DISCONTINUITY_REASON_SEEK ||
			reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION

		val jumpedToStart = newPosition.positionMs < 5000L
		val wasFurtherAlong = oldPosition.positionMs > 10000L

		if (isSeekOrRepeat && jumpedToStart && wasFurtherAlong) {
			scrobbleManager.onMediaChanged(player.currentMediaItem?.mediaId)
		}
	}

	override fun onIsPlayingChanged(isPlaying: Boolean) {
		scrobbleManager.onPlayStateChanged(isPlaying)
	}

	fun release() {
		player.removeListener(this)
	}
}

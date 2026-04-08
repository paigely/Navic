package paige.navic.shared

import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.domain.repositories.CollectionRepository
import paige.navic.domain.repositories.PlayerStateRepository
import paige.navic.managers.ConnectivityManager
import paige.navic.managers.DownloadManager

// TODO: implement this class
class JvmMediaPlayerViewModel(
	stateRepository: PlayerStateRepository,
	collectionRepository: CollectionRepository,
	downloadManager: DownloadManager,
	connectivityManager: ConnectivityManager
) : MediaPlayerViewModel(
	stateRepository = stateRepository,
	collectionRepository = collectionRepository,
	downloadManager = downloadManager,
	connectivityManager = connectivityManager
) {
	override fun addToQueueSingle(song: DomainSong) {
	}

	override fun addToQueue(collection: DomainSongCollection) {
	}

	override fun removeFromQueue(index: Int) {
	}

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
	}

	override fun clearQueue() {
	}

	override fun playAt(index: Int) {
		resetSleepTimer()
	}

	override fun pause() {
	}

	override fun resume() {
		resetSleepTimer()
	}

	override fun seek(normalized: Float) {
		resetSleepTimer()
	}

	override fun next() {
		resetSleepTimer()
	}

	override fun previous() {
		resetSleepTimer()
	}

	override fun toggleShuffle() {
	}

	override fun toggleRepeat() {
	}

	override fun shufflePlay(collection: DomainSongCollection) {
		resetSleepTimer()
	}

	override fun syncPlayerWithState(state: PlayerUiState) {
	}
}

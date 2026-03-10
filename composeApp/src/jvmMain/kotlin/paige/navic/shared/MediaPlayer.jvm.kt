package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.api.model.SongCollection
import java.io.File

// TODO: implement this class
class JvmMediaPlayerViewModel(
	storage: PlayerStateStorage
) : MediaPlayerViewModel(storage) {
	override fun addToQueueSingle(track: Song) {
	}

	override fun addToQueue(tracks: SongCollection) {
	}

	override fun removeFromQueue(index: Int) {
	}

	override fun moveQueueItem(fromIndex: Int, toIndex: Int) {
	}

	override fun clearQueue() {
	}

	override fun playAt(index: Int) {
	}

	override fun pause() {
	}

	override fun resume() {
	}

	override fun seek(normalized: Float) {
	}

	override fun next() {
	}

	override fun previous() {
	}

	override fun toggleShuffle() {
	}

	override fun toggleRepeat() {
	}

	override fun shufflePlay(tracks: SongCollection) {
	}

	override fun syncPlayerWithState(state: PlayerUiState) {
	}
}

@Composable
actual fun rememberMediaPlayer(): MediaPlayerViewModel {
	return viewModel {
		val producePath = {
			val home = System.getProperty("user.home")
			val os = System.getProperty("os.name").lowercase()
			val directory = when {
				os.contains("linux") -> {
					val xdgConfig = System.getenv("XDG_CONFIG_HOME")
					if (!xdgConfig.isNullOrBlank()) {
						File(xdgConfig, "navic")
					} else {
						File(home, ".config/navic")
					}
				}
				os.contains("mac") -> File(home, "Library/Application Support/Navic")
				os.contains("win") -> File(System.getenv("AppData"), "Navic")
				else -> File(home, ".navic")
			}
			if (!directory.exists()) directory.mkdirs()
			File(directory, DATASTORE_FILE_NAME).absolutePath
		}

		val dataStore = DataStoreSingleton.getInstance(producePath)
		val storage = DataStorePlayerStorage(dataStore)

		JvmMediaPlayerViewModel(storage)
	}
}

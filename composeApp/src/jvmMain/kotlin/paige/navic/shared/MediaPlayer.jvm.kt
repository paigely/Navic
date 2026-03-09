package paige.navic.shared

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.SongCollection

// TODO: implement this class
class JvmMediaPlayerViewModel : MediaPlayerViewModel() {
	override fun play(tracks: SongCollection, startIndex: Int) {
	}

	override fun playSingle(track: Track) {
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

	override fun shufflePlay(tracks: SongCollection) {
	}

	override fun toggleRepeat() {
	}
}

@Composable
actual fun rememberMediaPlayer(): MediaPlayerViewModel {
	return viewModel { JvmMediaPlayerViewModel() }
}
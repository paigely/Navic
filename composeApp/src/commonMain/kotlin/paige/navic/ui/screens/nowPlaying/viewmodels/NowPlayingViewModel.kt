package paige.navic.ui.screens.nowPlaying.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import paige.navic.domain.repositories.SongRepository
import paige.navic.shared.MediaPlayerViewModel

class NowPlayingViewModel(
	private val songRepository: SongRepository
) : ViewModel(), KoinComponent {
	private val player: MediaPlayerViewModel by inject()

	private val _songIsStarred = MutableStateFlow(false)
	val songIsStarred = _songIsStarred.asStateFlow()

	private val _songRating = MutableStateFlow(0)
	val songRating = _songRating.asStateFlow()

	init {
		viewModelScope.launch {
			player.uiState.collect { state ->
				state.currentSong?.let { song ->
					_songIsStarred.value = songRepository.isSongStarred(song)
					_songRating.value = songRepository.getSongRating(song)
				}
			}
		}
	}

	fun starSong(starred: Boolean) {
		viewModelScope.launch {
			runCatching {
				player.uiState.value.currentSong?.let { song ->
					_songIsStarred.value = starred
					if (starred) {
						songRepository.starSong(song)
					} else {
						songRepository.unstarSong(song)
					}
				}
			}
		}
	}

	fun rateSong(rating: Int) {
		viewModelScope.launch {
			runCatching {
				player.uiState.value.currentSong?.let { song ->
					_songRating.value = rating
					songRepository.rateSong(song, rating)
				}
			}
		}
	}
}

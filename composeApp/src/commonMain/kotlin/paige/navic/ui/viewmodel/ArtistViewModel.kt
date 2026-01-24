package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.util.UiState
import paige.subsonic.api.model.Artist
import paige.subsonic.api.model.Song

class ArtistViewModel(
	private val artistId: String
) : ViewModel() {
	private val _artistState = MutableStateFlow<UiState<Artist>>(UiState.Loading)
	val artistState = _artistState.asStateFlow()
	private val _topSongs = MutableStateFlow<List<Song>>(emptyList())
	val topSongs = _topSongs.asStateFlow()

	init {
		viewModelScope.launch {
			try {
				val artist = SessionManager.api.getArtist(artistId).data.artist.let {
					it.copy(
						coverArt = SessionManager.api
							.getCoverArtUrl(it.coverArt, size = 512, auth = true),
						album = it.album.orEmpty().map { album ->
							album.copy(
								coverArt = SessionManager.api
									.getCoverArtUrl(album.coverArt, size = 512, auth = true)
							)
						}
					)
				}
				val topSongs = SessionManager.api.getTopSongs(artist.name).data.topSongs.let {
					it.copy(
						song = it.song?.map { song ->
							song.copy(
								coverArt = SessionManager.api
									.getCoverArtUrl(song.coverArt, size = 128, auth = true)
							)
						}
					)
				}
				_topSongs.value = topSongs.song.orEmpty()
				_artistState.value = UiState.Success(artist)
			} catch (e: Exception) {
				_artistState.value = UiState.Error(e)
			}
		}
	}
}
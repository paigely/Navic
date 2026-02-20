package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import paige.subsonic.api.models.Artist
import paige.subsonic.api.models.ArtistInfo
import paige.subsonic.api.models.Track

data class ArtistState(
	val artist: Artist,
	val topSongs: List<Track>,
	val info: ArtistInfo
)

class ArtistViewModel(
	private val artistId: String
) : ViewModel() {
	private val _artistState = MutableStateFlow<UiState<ArtistState>>(UiState.Loading)
	val artistState = _artistState.asStateFlow()

	init {
		viewModelScope.launch {
			try {
				val artist = SessionManager.api.getArtist(artistId).data.artist
				val topSongs = SessionManager.api.getTopSongs(artist.name).data.topSongs.song.orEmpty()
				val artistInfo = SessionManager.api.getArtistInfo(artist.id).data.artistInfo
				_artistState.value = UiState.Success(ArtistState(
					artist,
					topSongs,
					artistInfo
				))
			} catch (e: Exception) {
				_artistState.value = UiState.Error(e)
			}
		}
	}
}
package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repositories.ArtistsRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import paige.subsonic.api.models.Artist
import paige.subsonic.api.models.Artists

class ArtistsViewModel(
	private val repository: ArtistsRepository = ArtistsRepository()
) : ViewModel() {
	private val _artistsState = MutableStateFlow<UiState<List<Artists.Index>>>(UiState.Loading)
	val artistsState = _artistsState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	private val _selectedArtist = MutableStateFlow<Artist?>(null)
	val selectedArtist: StateFlow<Artist?> = _selectedArtist.asStateFlow()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshArtists()
			}
		}
	}

	fun refreshArtists() {
		viewModelScope.launch {
			_artistsState.value = UiState.Loading
			try {
				val artists = repository.getArtists()
				_artistsState.value = UiState.Success(artists)
			} catch (e: Exception) {
				_artistsState.value = UiState.Error(e)
			}
		}
	}

	fun selectArtist(artist: Artist) {
		viewModelScope.launch {
			_selectedArtist.value = artist
			_starredState.value = UiState.Loading
			try {
				val isStarred = repository.isArtistStarred(artist)
				_starredState.value = UiState.Success(isStarred ?: false)
			} catch(e: Exception) {
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun clearSelection() {
		_selectedArtist.value = null
	}

	fun starSelectedArtist() {
		viewModelScope.launch {
			try {
				repository.starArtist(_selectedArtist.value!!)
			} catch(_: Exception) { }
		}
	}

	fun unstarSelectedArtist() {
		viewModelScope.launch {
			try {
				repository.unstarArtist(_selectedArtist.value!!)
			} catch(_: Exception) { }
		}
	}
}
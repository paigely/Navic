package paige.navic.ui.screens.artist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.repositories.ArtistListType
import paige.navic.domain.repositories.ArtistRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainArtist
import paige.navic.shared.Logger
import paige.navic.utils.UiState

class ArtistListViewModel(
	private val repository: ArtistRepository
) : ViewModel() {
	private val _artistsState = MutableStateFlow<UiState<ImmutableList<DomainArtist>>>(UiState.Loading())
	val artistsState = _artistsState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	private val _selectedArtist = MutableStateFlow<DomainArtist?>(null)
	val selectedArtist = _selectedArtist.asStateFlow()

	private val _listType = MutableStateFlow(ArtistListType.AlphabeticalByName)
	val listType = _listType.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshArtists(false) }
		}
	}

	fun refreshArtists(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getArtistsFlow(fullRefresh, _listType.value).collect {
				_artistsState.value = it
			}
		}
	}

	fun selectArtist(artist: DomainArtist) {
		viewModelScope.launch {
			_selectedArtist.value = artist
			_starredState.value = UiState.Loading()
			try {
				val isStarred = repository.isArtistStarred(artist)
				_starredState.value = UiState.Success(isStarred)
			} catch(e: Exception) {
				Logger.e("ArtistListViewModel", "Failed to get star state of artist", e)
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun clearSelection() {
		_selectedArtist.value = null
	}

	fun starArtist(starred: Boolean) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			try {
				if (starred) {
					repository.unstarArtist(artist)
				} else {
					repository.starArtist(artist)
				}
				_starredState.value = UiState.Success(true)
			} catch(e: Exception) {
				Logger.e("ArtistListViewModel", "Failed to star artist", e)
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun clearError() {
		_artistsState.value = UiState.Success(_artistsState.value.data ?: persistentListOf())
	}
}
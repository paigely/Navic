package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.LibraryRepository
import paige.navic.data.session.SessionManager
import paige.navic.util.UiState
import paige.subsonic.api.model.Album

class LibraryViewModel(
	private val repository: LibraryRepository = LibraryRepository()
) : ViewModel() {
	private val _albumsState = MutableStateFlow<UiState<List<Album>>>(UiState.Loading)
	val albumsState = _albumsState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	private val _selectedAlbum = MutableStateFlow<Album?>(null)
	val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect {
				refreshAlbums()
			}
		}
	}

	fun refreshAlbums() {
		viewModelScope.launch {
			_albumsState.value = UiState.Loading
			try {
				val albums = repository.getAlbums()
				_albumsState.value = UiState.Success(albums)
			} catch (e: Exception) {
				_albumsState.value = UiState.Error(e)
			}
		}
	}

	fun selectAlbum(album: Album) {
		viewModelScope.launch {
			_selectedAlbum.value = album
			_starredState.value = UiState.Loading
			try {
				val isStarred = repository.isAlbumStarred(album)
				_starredState.value = UiState.Success(isStarred ?: false)
			} catch(e: Exception) {
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun clearSelection() {
		_selectedAlbum.value = null
	}

	fun starSelectedAlbum() {
		viewModelScope.launch {
			try {
				repository.starAlbum(_selectedAlbum.value!!)
			} catch(_: Exception) { }
		}
	}

	fun unstarSelectedAlbum() {
		viewModelScope.launch {
			try {
				repository.unstarAlbum(_selectedAlbum.value!!)
			} catch(_: Exception) { }
		}
	}
}

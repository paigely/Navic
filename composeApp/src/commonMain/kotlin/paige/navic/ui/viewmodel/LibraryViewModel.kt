package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
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
}

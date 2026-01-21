package paige.navic.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.AlbumsRepository
import paige.navic.data.session.SessionManager
import paige.navic.util.UiState
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.ListType

open class AlbumsViewModel(
	private val initialListType: ListType = ListType.ALPHABETICAL_BY_ARTIST,
	private val repository: AlbumsRepository = AlbumsRepository()
) : ViewModel() {
	private val _albumsState = MutableStateFlow<UiState<List<Album>>>(UiState.Loading)
	val albumsState = _albumsState.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	private val _selectedAlbum = MutableStateFlow<Album?>(null)
	val selectedAlbum: StateFlow<Album?> = _selectedAlbum.asStateFlow()

	private val _listType = MutableStateFlow(initialListType)
	val listType = _listType.asStateFlow()

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
				val albums = repository.getAlbums(_listType.value)
				_albumsState.value = UiState.Success(albums)
			} catch (e: Exception) {
				_albumsState.value = UiState.Error(e)
			}
		}
	}

	fun selectAlbum(album: Album?) {
		viewModelScope.launch {
			_selectedAlbum.value = album
			if (album == null) return@launch
			_starredState.value = UiState.Loading
			try {
				val isStarred = repository.isAlbumStarred(album)
				_starredState.value = UiState.Success(isStarred ?: false)
			} catch(e: Exception) {
				_starredState.value = UiState.Error(e)
			}
		}
	}

	fun starAlbum(starred: Boolean) {
		viewModelScope.launch {
			val selection = _selectedAlbum.value ?: return@launch
			runCatching {
				if (starred) {
					repository.starAlbum(selection)
				} else {
					repository.unstarAlbum(selection)
				}
			}
		}
	}

	fun setListType(listType: ListType) {
		_listType.value = listType
	}
}

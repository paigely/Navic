package paige.navic.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.models.Settings
import paige.navic.data.repositories.AlbumsRepository
import paige.navic.data.session.SessionManager
import paige.navic.utils.UiState
import paige.subsonic.api.models.Album
import paige.subsonic.api.models.ListType

open class AlbumsViewModel(
	initialListType: ListType?,
	private val repository: AlbumsRepository = AlbumsRepository()
) : ViewModel() {
	private val _albumsState = MutableStateFlow<UiState<List<Album>>>(UiState.Loading)
	val albumsState = _albumsState.asStateFlow()

	private val _isRefreshing = MutableStateFlow(false)
	val isRefreshing = _isRefreshing.asStateFlow()

	private val _starredState = MutableStateFlow<UiState<Boolean>>(UiState.Success(false))
	val starredState = _starredState.asStateFlow()

	private val _selectedAlbum = MutableStateFlow<Album?>(null)
	val selectedAlbum = _selectedAlbum.asStateFlow()

	private val _offset = MutableStateFlow(0)
	private val _isPaginating = MutableStateFlow(false)
	val isPaginating: StateFlow<Boolean> = _isPaginating

	private val _listType = MutableStateFlow(initialListType ?: Settings.shared.listType)
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
			_offset.value = 0

			val currentState = _albumsState.value
			val hasExistingData = currentState is UiState.Success && currentState.data.isNotEmpty()

			if (hasExistingData) {
				_isRefreshing.value = true
			} else {
				_albumsState.value = UiState.Loading
			}

			try {
				val albums = repository.getAlbums(listType = _listType.value, offset = _offset.value)
				_albumsState.value = UiState.Success(albums)
			} catch (e: Exception) {
				if (!hasExistingData) {
					_albumsState.value = UiState.Error(e)
				}
			} finally {
				_isRefreshing.value = false
			}
		}
	}

	fun paginate() {
		if (_albumsState.value !is UiState.Success || _isPaginating.value) return

		viewModelScope.launch {
			val newOffset = _offset.value + 30
			_isPaginating.value = true
			try {
				val newAlbums = repository.getAlbums(listType = _listType.value, offset = newOffset)
				_albumsState.value = (_albumsState.value as UiState.Success).let {
					it.copy(data = it.data + newAlbums)
				}
				_offset.value = newOffset
			} finally {
				_isPaginating.value = false
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
		Settings.shared.listType = listType
	}
}

package paige.navic.ui.screens.album.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.repositories.AlbumRepository
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.managers.ConnectivityManager
import paige.navic.utils.UiState

@OptIn(ExperimentalCoroutinesApi::class)
open class AlbumListViewModel(
	initialListType: DomainAlbumListType = DomainAlbumListType.AlphabeticalByArtist,
	private val repository: AlbumRepository,
	private val connectivityManager: ConnectivityManager
) : ViewModel() {
	private val _albumsState = MutableStateFlow<UiState<ImmutableList<DomainAlbum>>>(UiState.Loading())
	val albumsState = _albumsState.asStateFlow()

	private val _selectedAlbum = MutableStateFlow<DomainAlbum?>(null)
	val selectedAlbum = _selectedAlbum.asStateFlow()

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _listType = MutableStateFlow(initialListType)
	val listType = _listType.asStateFlow()

	private val _selectedReversed = MutableStateFlow(false)
	val selectedReversed = _selectedReversed.asStateFlow()

	val isOnline = connectivityManager.isOnline

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshAlbums(false) }
		}
	}

	fun refreshAlbums(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getAlbumsFlow(fullRefresh, _listType.value, _selectedReversed.value).collect {
				_albumsState.value = it
			}
		}
	}

	fun selectAlbum(album: DomainAlbum) {
		viewModelScope.launch {
			_selectedAlbum.value = album
			_starred.value = repository.isAlbumStarred(album)
		}
	}

	fun clearSelection() {
		_selectedAlbum.value = null
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
				_starred.value = starred
			}
		}
	}

	fun setListType(listType: DomainAlbumListType) {
		_listType.value = listType
		refreshAlbums(false)
	}

	fun setReversed(reversed: Boolean) {
		_selectedReversed.value = reversed
		refreshAlbums(false)
	}

	fun clearError() {
		_albumsState.value = UiState.Success(_albumsState.value.data ?: persistentListOf())
	}
}
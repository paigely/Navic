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
import org.koin.core.component.KoinComponent
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumSummary
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.repositories.AlbumRepository
import paige.navic.ui.core.UiState

@OptIn(ExperimentalCoroutinesApi::class)
open class AlbumListViewModel(
	initialListType: DomainAlbumListType = DomainAlbumListType.AlphabeticalByArtist,
	private val repository: AlbumRepository,
	private val sessionManager: SessionManager
) : ViewModel(), KoinComponent {
	private val _albumsState =
		MutableStateFlow<UiState<ImmutableList<DomainAlbumSummary>>>(UiState.Loading())
	val albumsState = _albumsState.asStateFlow()

	private val _selectedAlbum = MutableStateFlow<DomainAlbum?>(null)
	val selectedAlbum = _selectedAlbum.asStateFlow()

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _rating = MutableStateFlow(0)
	val rating = _rating.asStateFlow()

	private val _listType = MutableStateFlow(initialListType)
	val listType = _listType.asStateFlow()

	private val _selectedReversed = MutableStateFlow(false)
	val selectedReversed = _selectedReversed.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshAlbums(false) }
		}
	}

	fun refreshAlbums(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getAlbumsFlow(fullRefresh, _listType.value, _selectedReversed.value)
				.collect {
					_albumsState.value = it
				}
		}
	}

	fun selectAlbum(album: DomainAlbumSummary) {
		viewModelScope.launch {
			val fullAlbum = repository.getAlbumById(album.id)
			_selectedAlbum.value = fullAlbum
			_starred.value = repository.isAlbumStarred(album.id)
			_rating.value = repository.getAlbumRating(album.id)
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
					repository.starAlbum(selection.id)
				} else {
					repository.unstarAlbum(selection.id)
				}
				_starred.value = starred
			}
		}
	}

	fun setRating(rating: Int) {
		viewModelScope.launch {
			val selection = _selectedAlbum.value ?: return@launch
			runCatching {
				_rating.value = rating
				repository.rateAlbum(selection.id, rating)
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

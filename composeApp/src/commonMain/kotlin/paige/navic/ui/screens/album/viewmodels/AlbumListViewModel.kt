package paige.navic.ui.screens.album.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.repositories.AlbumRepository
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
open class AlbumListViewModel(
	initialListType: DomainAlbumListType = DomainAlbumListType.AlphabeticalByArtist,
	private val repository: AlbumRepository
) : ViewModel() {
	private val _selectedAlbum = MutableStateFlow<DomainAlbum?>(null)
	val selectedAlbum = _selectedAlbum.asStateFlow()

	private val _error = MutableStateFlow<Throwable?>(null)
	val error = _error.asStateFlow()

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _rating = MutableStateFlow(0)
	val rating = _rating.asStateFlow()

	private val _listType = MutableStateFlow(initialListType)
	val listType = _listType.asStateFlow()

	private val _selectedReversed = MutableStateFlow(false)
	val selectedReversed = _selectedReversed.asStateFlow()

	private val _refreshTrigger = MutableStateFlow(Clock.System.now())

	val gridState = LazyGridState()

	val pagedAlbums: Flow<PagingData<DomainAlbum>> = combine(
		_listType,
		_selectedReversed,
		_refreshTrigger
	) { type, reversed, _ ->
		type to reversed
	}.flatMapLatest { (type, reversed) ->
		repository.getPagedAlbums(type, reversed)
	}.cachedIn(viewModelScope)

	init {
		viewModelScope.launch {
			SessionManager.isLoggedIn.collect { if (it) refreshAlbums(false) }
		}
	}

	fun refreshAlbums(fullRefresh: Boolean) {
		if (!fullRefresh) return
		viewModelScope.launch {
			try {
				repository.syncLibrary()
			} catch (e: Exception) {
				_error.value = e
			}
		}
	}

	fun selectAlbum(album: DomainAlbum) {
		viewModelScope.launch {
			_selectedAlbum.value = album
			_starred.value = repository.isAlbumStarred(album)
			_rating.value = repository.getAlbumRating(album)
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

	fun setRating(rating: Int) {
		viewModelScope.launch {
			val selection = _selectedAlbum.value ?: return@launch
			runCatching {
				_rating.value = rating
				repository.rateAlbum(selection, rating)
			}
		}
	}

	fun setListType(listType: DomainAlbumListType) {
		_listType.value = listType
	}

	fun setReversed(reversed: Boolean) {
		_selectedReversed.value = reversed
	}

	fun clearError() {
		_error.value = null
	}
}

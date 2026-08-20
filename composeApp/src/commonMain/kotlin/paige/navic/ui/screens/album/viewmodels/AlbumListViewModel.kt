package paige.navic.ui.screens.album.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainFilter
import paige.navic.domain.models.toBitmask
import paige.navic.domain.models.toDomainFilters
import paige.navic.domain.repositories.AlbumRepository
import paige.navic.ui.core.UiState

class AlbumListViewModel(
	initialListType: DomainAlbumListType = DomainAlbumListType.AlphabeticalByArtist,
	initialFilters: Set<DomainFilter>? = null,
	private val repository: AlbumRepository,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) : ViewModel() {
	val albumsState: StateFlow<UiState<ImmutableList<DomainAlbum>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainAlbum>>>(UiState.Loading())

	val selectedAlbum: StateFlow<DomainAlbum?>
		field = MutableStateFlow(null)

	val starred: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val rating: StateFlow<Int>
		field = MutableStateFlow(0)

	val listType: StateFlow<DomainAlbumListType>
		field = MutableStateFlow(initialListType)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val selectedFilters: StateFlow<Set<DomainFilter>>
		field = MutableStateFlow(
			initialFilters ?: preferenceManager.albumFilters.toDomainFilters()
		)

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshAlbums(false) }
		}
	}

	fun refreshAlbums(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getAlbumsFlow(
				fullRefresh,
				listType.value,
				selectedReversed.value,
				selectedFilters.value
			).collect {
				albumsState.value = it
			}
		}
	}

	fun selectAlbum(album: DomainAlbum) {
		viewModelScope.launch {
			selectedAlbum.value = album
			starred.value = repository.isAlbumStarred(album)
			rating.value = repository.getAlbumRating(album)
		}
	}

	fun clearSelection() {
		selectedAlbum.value = null
	}

	fun starAlbum(isStarred: Boolean) {
		viewModelScope.launch {
			val selection = selectedAlbum.value ?: return@launch
			runCatching {
				if (isStarred) {
					repository.starAlbum(selection)
				} else {
					repository.unstarAlbum(selection)
				}
				starred.value = isStarred
			}
		}
	}

	fun setRating(newRating: Int) {
		viewModelScope.launch {
			val selection = selectedAlbum.value ?: return@launch
			runCatching {
				rating.value = newRating
				repository.rateAlbum(selection, newRating)
			}
		}
	}

	fun setListType(newListType: DomainAlbumListType) {
		listType.value = newListType
		refreshAlbums(false)
	}

	fun setReversed(reversed: Boolean) {
		selectedReversed.value = reversed
		refreshAlbums(false)
	}

	fun toggleFilter(filter: DomainFilter) {
		val current = selectedFilters.value
		val newFilters = if (current.contains(filter)) {
			current - filter
		} else {
			current + filter
		}
		selectedFilters.value = newFilters
		preferenceManager.albumFilters = newFilters.toBitmask()
		refreshAlbums(false)
	}

	fun clearError() {
		albumsState.value = UiState.Success(albumsState.value.data ?: persistentListOf())
	}
}

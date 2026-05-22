package paige.navic.ui.screens.artist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.repositories.ArtistRepository
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.utils.UiState

class ArtistListViewModel(
	initialListType: DomainArtistListType = DomainArtistListType.AlphabeticalByName,
	private val repository: ArtistRepository,
	private val albumDao: AlbumDao,
	private val sessionManager: SessionManager
) : ViewModel() {
	private val _artistsState =
		MutableStateFlow<UiState<ImmutableList<DomainArtist>>>(UiState.Loading())
	val artistsState = _artistsState.asStateFlow()

	private val _starred = MutableStateFlow(false)
	val starred = _starred.asStateFlow()

	private val _selectedArtist = MutableStateFlow<DomainArtist?>(null)
	val selectedArtist = _selectedArtist.asStateFlow()

	private val _selectedArtistAlbums = MutableStateFlow<ImmutableList<DomainAlbum>?>(null)
	val selectedArtistAlbums = _selectedArtistAlbums.asStateFlow()

	private val _listType = MutableStateFlow(initialListType)
	val listType = _listType.asStateFlow()

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshArtists(false) }
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
			val artistAlbums = 
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			_selectedArtistAlbums.value = artistAlbums.map { it.toDomainModel() }.toImmutableList()
			_starred.value = repository.isArtistStarred(artist)
		}
	}

	fun clearSelection() {
		_selectedArtist.value = null
	}

	fun starArtist(starred: Boolean) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			runCatching {
				if (starred) {
					repository.starArtist(artist)
				} else {
					repository.unstarArtist(artist)
				}
				_starred.value = starred
			}
		}
	}

	fun addArtistAlbumsToQueue(player: MediaPlayerViewModel) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			val artistAlbums = 
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.addToQueue(album)
			}
		}
	}

	fun playArtistAlbumsNext(player: MediaPlayerViewModel) {
		val artist = _selectedArtist.value ?: return
		viewModelScope.launch {
			val artistAlbums = 
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.playNext(album)
			}
		}
	}

	// TODO: implement me
	fun setListType(listType: DomainArtistListType) {
		_listType.value = listType
	}

	fun clearError() {
		_artistsState.value = UiState.Success(_artistsState.value.data ?: persistentListOf())
	}
}

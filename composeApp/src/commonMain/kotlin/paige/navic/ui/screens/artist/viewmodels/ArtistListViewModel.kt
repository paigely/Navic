package paige.navic.ui.screens.artist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.repositories.ArtistRepository
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.core.UiState

class ArtistListViewModel(
	initialListType: DomainArtistListType = DomainArtistListType.AlphabeticalByName,
	private val repository: ArtistRepository,
	private val albumDao: AlbumDao,
	private val sessionManager: SessionManager
) : ViewModel() {
	val artistsState: StateFlow<UiState<ImmutableList<DomainArtist>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainArtist>>>(UiState.Loading())

	val starred: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val selectedArtist: StateFlow<DomainArtist?>
		field = MutableStateFlow(null)

	val selectedArtistAlbums: StateFlow<ImmutableList<DomainAlbum>?>
		field = MutableStateFlow(null)

	val listType: StateFlow<DomainArtistListType>
		field = MutableStateFlow(initialListType)

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshArtists(false) }
		}
	}

	fun refreshArtists(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getArtistsFlow(fullRefresh, listType.value).collect {
				artistsState.value = it
			}
		}
	}

	fun selectArtist(artist: DomainArtist) {
		viewModelScope.launch {
			selectedArtist.value = artist
			val artistAlbums =
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			selectedArtistAlbums.value = artistAlbums.map { it.toDomainModel() }.toImmutableList()
			starred.value = repository.isArtistStarred(artist)
		}
	}

	fun clearSelection() {
		selectedArtist.value = null
	}

	fun starArtist(isStarred: Boolean) {
		val artist = selectedArtist.value ?: return
		viewModelScope.launch {
			runCatching {
				if (isStarred) {
					repository.starArtist(artist)
				} else {
					repository.unstarArtist(artist)
				}
				starred.value = isStarred
			}
		}
	}

	fun addArtistAlbumsToQueue(player: MediaPlayerViewModel) {
		val artist = selectedArtist.value ?: return
		viewModelScope.launch {
			val artistAlbums =
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.addToQueue(album)
			}
		}
	}

	fun playArtistAlbumsNext(player: MediaPlayerViewModel) {
		val artist = selectedArtist.value ?: return
		viewModelScope.launch {
			val artistAlbums =
				albumDao.getAlbumsByArtist(artist.id).firstOrNull() ?: emptyList()
			artistAlbums.map { it.toDomainModel() }.forEach { album ->
				player.playNext(album)
			}
		}
	}

	// TODO: implement me
	fun setListType(newListType: DomainArtistListType) {
		listType.value = newListType
	}

	fun clearError() {
		artistsState.value = UiState.Success(artistsState.value.data ?: persistentListOf())
	}
}

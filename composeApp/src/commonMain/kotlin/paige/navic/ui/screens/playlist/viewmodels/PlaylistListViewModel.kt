package paige.navic.ui.screens.playlist.viewmodels

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.domain.repositories.PlaylistRepository
import paige.navic.ui.core.UiState

class PlaylistListViewModel(
	private val repository: PlaylistRepository,
	private val sessionManager: SessionManager
) : ViewModel() {
	val playlistsState: StateFlow<UiState<ImmutableList<DomainPlaylist>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainPlaylist>>>(UiState.Loading())

	val selectedPlaylist: StateFlow<DomainPlaylist?>
		field = MutableStateFlow(null)

	val selectedSorting: StateFlow<DomainPlaylistListType>
		field = MutableStateFlow(DomainPlaylistListType.DateAdded)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val gridState = LazyGridState()

	init {
		viewModelScope.launch {
			sessionManager.isLoggedIn.collect { if (it) refreshPlaylists(false) }
		}
	}

	fun selectPlaylist(playlist: DomainPlaylist) {
		selectedPlaylist.value = playlist
	}

	fun clearSelection() {
		selectedPlaylist.value = null
	}

	fun refreshPlaylists(fullRefresh: Boolean) {
		viewModelScope.launch {
			repository.getPlaylistsFlow(
				fullRefresh,
				selectedSorting.value,
				selectedReversed.value
			).collect {
				playlistsState.value = it
			}
		}
	}

	fun setSorting(sorting: DomainPlaylistListType) {
		selectedSorting.value = sorting
		refreshPlaylists(false)
	}

	fun setReversed(reversed: Boolean) {
		selectedReversed.value = reversed
		refreshPlaylists(false)
	}

	fun clearError() {
		playlistsState.value = UiState.Success(playlistsState.value.data ?: persistentListOf())
	}
}

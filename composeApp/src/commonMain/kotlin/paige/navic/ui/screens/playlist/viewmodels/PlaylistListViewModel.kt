package paige.navic.ui.screens.playlist.viewmodels

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
import paige.navic.domain.models.DomainFilter
import paige.navic.domain.models.DomainPlaylist
import paige.navic.domain.models.DomainPlaylistListType
import paige.navic.domain.models.toBitmask
import paige.navic.domain.models.toDomainFilters
import paige.navic.domain.repositories.PlaylistRepository
import paige.navic.ui.core.UiState

class PlaylistListViewModel(
	private val repository: PlaylistRepository,
	private val sessionManager: SessionManager,
	private val preferenceManager: PreferenceManager
) : ViewModel() {
	val playlistsState: StateFlow<UiState<ImmutableList<DomainPlaylist>>>
		field = MutableStateFlow<UiState<ImmutableList<DomainPlaylist>>>(UiState.Loading())

	val selectedPlaylist: StateFlow<DomainPlaylist?>
		field = MutableStateFlow(null)

	val selectedSorting: StateFlow<DomainPlaylistListType>
		field = MutableStateFlow(DomainPlaylistListType.DateAdded)

	val selectedReversed: StateFlow<Boolean>
		field = MutableStateFlow(false)

	val selectedFilters: StateFlow<Set<DomainFilter>>
		field = MutableStateFlow(preferenceManager.playlistFilters.toDomainFilters())

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
				selectedReversed.value,
				selectedFilters.value
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

	fun toggleFilter(filter: DomainFilter) {
		val current = selectedFilters.value
		val newFilters = if (current.contains(filter)) {
			current - filter
		} else {
			current + filter
		}
		selectedFilters.value = newFilters
		preferenceManager.playlistFilters = newFilters.toBitmask()
		refreshPlaylists(false)
	}

	fun clearError() {
		playlistsState.value = UiState.Success(playlistsState.value.data ?: persistentListOf())
	}
}

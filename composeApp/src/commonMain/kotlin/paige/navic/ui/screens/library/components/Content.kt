package paige.navic.ui.screens.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_sort_frequent
import navic.composeapp.generated.resources.option_sort_newest
import navic.composeapp.generated.resources.option_sort_random
import navic.composeapp.generated.resources.option_sort_recent
import navic.composeapp.generated.resources.option_sort_starred
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_genres
import navic.composeapp.generated.resources.title_playlists
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainGenre
import paige.navic.domain.models.DomainPlaylist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.History
import paige.navic.icons.outlined.LibraryAdd
import paige.navic.icons.outlined.Shuffle
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.layouts.horizontalSection
import paige.navic.ui.components.layouts.pagedHorizontalSection
import paige.navic.ui.screens.album.components.AlbumListScreenItem
import paige.navic.ui.screens.artist.ArtistsScreenItem
import paige.navic.ui.screens.genre.components.GenreListScreenCard
import paige.navic.ui.screens.playlist.components.PlaylistListScreenItem
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenContent(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	onSetShareId: (String) -> Unit,
	isOnline: Boolean,

	// albums
	pagedAlbums: LazyPagingItems<DomainAlbum>,
	selectedAlbum: DomainAlbum?,
	selectedAlbumIsStarred: Boolean,
	onSelectAlbum: (DomainAlbum) -> Unit,
	onClearAlbumSelection: () -> Unit,
	onStarSelectedAlbum: (Boolean) -> Unit,

	// artists
	artistsState: UiState<ImmutableList<DomainArtist>>,
	selectedArtist: DomainArtist?,
	selectedArtistIsStarred: Boolean,
	onSelectArtist: (DomainArtist) -> Unit,
	onClearArtistSelection: () -> Unit,
	onStarSelectedArtist: (Boolean) -> Unit,
	onAddAlbumToQueue: () -> Unit,

	// playlists
	playlistsState: UiState<ImmutableList<DomainPlaylist>>,
	selectedPlaylist: DomainPlaylist?,
	onSelectPlaylist: (DomainPlaylist) -> Unit,
	onClearPlaylistSelection: () -> Unit,
	onDeletePlaylist: (String) -> Unit,
	onAddPlaylistToQueue: () -> Unit,

	// genres
	genresState: UiState<ImmutableList<DomainGenre>>
) {
	LazyVerticalGrid(
		modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
		columns = GridCells.Fixed(2),
		contentPadding = innerPadding.withoutTop() + PaddingValues(top = 8.dp),
		verticalArrangement = Arrangement.spacedBy(5.dp),
		horizontalArrangement = Arrangement.spacedBy(5.dp),
	) {
		libraryScreenOverviewButton(
			icon = Icons.Outlined.LibraryAdd,
			label = Res.string.option_sort_newest,
			destination = Screen.AlbumList(true, DomainAlbumListType.Newest),
			start = true
		)
		libraryScreenOverviewButton(
			icon = Icons.Outlined.Shuffle,
			label = Res.string.option_sort_random,
			destination = Screen.AlbumList(true, DomainAlbumListType.Random),
			start = false
		)
		libraryScreenOverviewButton(
			icon = Icons.Outlined.Star,
			label = Res.string.option_sort_starred,
			destination = Screen.AlbumList(true, DomainAlbumListType.Starred),
			start = true
		)
		libraryScreenOverviewButton(
			icon = Icons.Outlined.History,
			label = Res.string.option_sort_frequent,
			destination = Screen.AlbumList(true, DomainAlbumListType.Frequent),
			start = false
		)

		pagedHorizontalSection(
			title = Res.string.option_sort_recent,
			destination = Screen.AlbumList(true, DomainAlbumListType.Recent),
			items = pagedAlbums,
			seeAll = true
		) { album ->
			AlbumListScreenItem(
				modifier = Modifier.animateItem().width(150.dp),
				tab = "library",
				album = album,
				selected = album == selectedAlbum,
				starred = selectedAlbumIsStarred,
				onSelect = { onSelectAlbum(album) },
				onDeselect = { onClearAlbumSelection() },
				onSetStarred = { onStarSelectedAlbum(it) },
				onSetShareId = { onSetShareId(it) },
				onAddToQueue = onAddAlbumToQueue,
				isOnline = isOnline
			)
		}

		horizontalSection(
			title = Res.string.title_playlists,
			destination = Screen.PlaylistList(true),
			state = playlistsState,
			key = { it.id },
			seeAll = true
		) { playlist ->
			PlaylistListScreenItem(
				modifier = Modifier.animateItem().width(150.dp),
				tab = "library",
				playlist = playlist,
				selected = playlist == selectedPlaylist,
				onSelect = { onSelectPlaylist(playlist) },
				onDeselect = { onClearPlaylistSelection() },
				onSetDeletionId = { onDeletePlaylist(it) },
				onSetShareId = { onSetShareId(it) },
				onAddToQueue = onAddPlaylistToQueue
			)
		}

		horizontalSection(
			title = Res.string.title_artists,
			destination = Screen.ArtistList(true),
			state = artistsState,
			key = { it.id },
			seeAll = true
		) { artist ->
			ArtistsScreenItem(
				modifier = Modifier.animateItem().width(150.dp),
				tab = "library",
				artist = artist,
				selected = artist == selectedArtist,
				starred = selectedArtistIsStarred,
				onSelect = { onSelectArtist(artist) },
				onDeselect = { onClearArtistSelection() },
				onSetStarred = { onStarSelectedArtist(it) }
			)
		}

		horizontalSection(
			title = Res.string.title_genres,
			destination = Screen.GenreList(true),
			state = genresState,
			key = { it.name },
			seeAll = true
		) { genreWithAlbums ->
			GenreListScreenCard(genre = genreWithAlbums)
		}
	}
}

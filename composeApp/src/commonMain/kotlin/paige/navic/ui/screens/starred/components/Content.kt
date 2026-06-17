package paige.navic.ui.screens.starred.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import navic.composeapp.generated.resources.count_albums
import navic.composeapp.generated.resources.info_no_starred
import navic.composeapp.generated.resources.title_albums
import navic.composeapp.generated.resources.title_artists
import navic.composeapp.generated.resources.title_songs
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalPlatformContext
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.ui.navigation.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainArtist
import paige.navic.domain.models.DomainArtistListType
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.domain.manager.DownloadManager
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.common.SongRow
import paige.navic.ui.components.layouts.ArtCarousel
import paige.navic.ui.components.layouts.ArtCarouselItem
import paige.navic.ui.components.sheets.ArtistSheet
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.ui.core.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StarredScreenContent(
	innerPadding: PaddingValues,
	onSetShareId: (String) -> Unit,
	isOnline: Boolean = false,

	songsState: UiState<ImmutableList<DomainSong>>,
	selectedSong: DomainSong?,
	selectedSongIsStarred: Boolean,
	selectedSongRating: Int,
	allDownloads: ImmutableList<DownloadEntity>,
	onSelectSong: (DomainSong) -> Unit,
	onClearSongSelection: () -> Unit,
	onAddSongStar: () -> Unit,
	onRemoveSongStar: () -> Unit,
	onPlaySongNext: (DomainSong) -> Unit,
	onAddSongToQueue: (DomainSong) -> Unit,
	onPlaySong: (Int) -> Unit,
	onSetSongRating: (Int) -> Unit,
	onDownloadSong: (DomainSong) -> Unit,
	onCancelDownloadSong: (DomainSong) -> Unit,
	onDeleteDownloadSong: (DomainSong) -> Unit,

	// albums
	albumsState: UiState<ImmutableList<DomainAlbum>>,
	selectedAlbum: DomainAlbum?,
	selectedAlbumIsStarred: Boolean,
	selectedAlbumRating: Int,
	onSelectAlbum: (DomainAlbum) -> Unit,
	onClearAlbumSelection: () -> Unit,
	onStarSelectedAlbum: (Boolean) -> Unit,
	onRateSelectedAlbum: (Int) -> Unit,
	onPlayAlbumNext: () -> Unit,
	onAddAlbumToQueue: () -> Unit,

	// artists
	artistsState: UiState<ImmutableList<DomainArtist>>,
	selectedArtist: DomainArtist?,
	selectedArtistAlbums: ImmutableList<DomainAlbum>?,
	selectedArtistIsStarred: Boolean,
	onSelectArtist: (DomainArtist) -> Unit,
	onClearArtistSelection: () -> Unit,
	onStarSelectedArtist: (Boolean) -> Unit,
	onPlayArtistNext: () -> Unit,
	onAddArtistToQueue: () -> Unit,
) {
	val gridState = rememberLazyGridState()
	val platformContext = LocalPlatformContext.current
	val backStack = LocalNavStack.current
	val albums = albumsState.data.orEmpty()
	val songs = songsState.data.orEmpty()
	val artists = artistsState.data.orEmpty()
	val downloadManager = koinInject<DownloadManager>()
	val uriHandler = LocalUriHandler.current

	val scope = rememberCoroutineScope()

	val layoutDirection = LocalLayoutDirection.current

	var songsToAddToPlaylist by rememberSaveable { mutableStateOf<ImmutableList<DomainSong>?>(null) }

	val scrollState = rememberScrollState()

	Column( 
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(state = scrollState),
		verticalArrangement = Arrangement.spacedBy(
			space = 12.dp,
			alignment = if (albums.isEmpty() && songs.isEmpty() && artists.isEmpty()) Alignment.CenterVertically else Alignment.Top
		),
		horizontalAlignment = Alignment.CenterHorizontally,
	) {
		if (albums.isEmpty() && songs.isEmpty() && artists.isEmpty()) {
			ContentUnavailable(
				icon = Icons.Outlined.PlaylistRemove,
				label = stringResource(Res.string.info_no_starred)
			)
			return@Column
		}
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					start = innerPadding.calculateStartPadding(
						layoutDirection
					)
				)
				.padding(
					end = innerPadding.calculateEndPadding(
						layoutDirection
					)
				)
				.padding(
					bottom = innerPadding.calculateBottomPadding()
				),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			if (!songs.isEmpty()) {
				Row(
					modifier = Modifier
						.heightIn(min = 32.dp)
						.padding(top = 8.dp)
						.padding(horizontal = 16.dp)
						.fillMaxWidth(),
					verticalAlignment = Alignment.CenterVertically,
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					Text(
						stringResource(Res.string.title_songs),
						style = MaterialTheme.typography.titleMediumEmphasized,
						fontWeight = FontWeight(600)
					)
					Text(
						stringResource(Res.string.action_see_all),
						style = MaterialTheme.typography.labelLarge,
						color = MaterialTheme.colorScheme.primary,
						modifier = Modifier.clickable(onClick = dropUnlessResumed {
							platformContext.clickSound()
							backStack.add(
								Screen.SongList(
									nested = true,
									listType = DomainSongListType.Starred
								)
							)
						})
					)
				}
				val rowCount = remember(songs.size) {
					songs.size.coerceIn(1, 3)
				}
				val gridHeight = remember(rowCount) {
					when (rowCount) {
						1 -> 82.dp
						2 -> 164.dp
						else -> 246.dp
					}
				}
				LazyHorizontalGrid(
					rows = GridCells.Fixed(rowCount),
					state = gridState,
					flingBehavior = rememberSnapFlingBehavior(lazyGridState = gridState),
					modifier = Modifier.fillMaxWidth().height(gridHeight)
				) {
					itemsIndexed(if (songs.size > 12) songs.slice(0..11) else songs) { index, song ->
						val download = allDownloads.find { it.songId == song.id }
						SongRow(
							modifier = Modifier.weight(1f),
							song = song,
							selected = selectedSong == song,
							onClick = { onPlaySong(index) },
							onLongClick = { onSelectSong(song) },
							onDismissRequest = { onClearSongSelection() },
							starredState = if (selectedSong == song) selectedSongIsStarred else song.starredAt != null,
							onAddStar = onAddSongStar,
							onRemoveStar = onRemoveSongStar,
							download = download,
							onDownload = { onDownloadSong(song) },
							onCancelDownload = { onCancelDownloadSong(song) },
							onDeleteDownload = { onDeleteDownloadSong(song) },
							onPlayNext = { onPlaySongNext(song) },
							onAddToQueue = { onAddSongToQueue(song) },
							onShare = { onSetShareId(song.id) },
							isOnline = isOnline,
							rating = selectedSongRating,
							onSetRating = { onSetSongRating(it) }
						)
					}
				}
			}
			ArtCarousel(
				stringResource(Res.string.title_albums),
				albums.toImmutableList(),
				Screen.AlbumList(true, DomainAlbumListType.Starred)
			) { album ->
				val albumDownloadStatus by downloadManager
					.getCollectionDownloadStatus(album.songs.map { it.id })
					.collectAsState(initial = DownloadStatus.NOT_DOWNLOADED)
				ArtCarouselItem(
					coverArtId = album.coverArtId, 
					title = album.name, 
					subtitle = album.artistName,
					contentDescription = null,
					onSelect = { onSelectAlbum(album) },
					onClick = dropUnlessResumed {
						backStack.add(Screen.CollectionDetail(album.id, "artist"))
					}
				)
				if (selectedAlbum == album) {
					CollectionSheet(
						onDismissRequest = { onClearAlbumSelection() },
						collection = album,
						starred = selectedAlbumIsStarred,
						onShare = { onSetShareId(album.id) },
						onPlayNext = onPlayAlbumNext,
						onAddToQueue = onAddAlbumToQueue,
						onSetStarred = { onStarSelectedAlbum(!selectedAlbumIsStarred) },
						onAddAllToPlaylist = { 
							songsToAddToPlaylist = selectedAlbum.songs.toImmutableList()
						},
						downloadStatus = albumDownloadStatus,
						onDownloadAll = { 
							scope.launch {
								downloadManager.downloadCollection(album)
							}
						},
						onCancelDownloadAll = {
							scope.launch {
								album.songs.forEach { downloadManager.cancelDownload(it.id) }
							}
						},
						onDeleteDownloadAll = {
							scope.launch {
								downloadManager.deleteDownloadedCollection(album)
							}
						},
						rating = selectedAlbumRating,
						onSetRating = onRateSelectedAlbum
					)
				}
			}
			if (artists.isEmpty()) return@Column
			ArtCarousel(
				stringResource(Res.string.title_artists),
				artists.toImmutableList(),
				Screen.ArtistList(true, DomainArtistListType.Starred)
			) { artist ->
				ArtCarouselItem(
					coverArtId = artist.coverArtId, 
					title = artist.name, 
					subtitle = pluralStringResource(
						Res.plurals.count_albums,
						artist.albumCount,
						artist.albumCount
					),
					contentDescription = null,
					onSelect = { onSelectArtist(artist) },
					onClick = dropUnlessResumed {
						backStack.add(Screen.ArtistDetail(artist.id))
					}
				)
				if (selectedArtist == artist) {
					ArtistSheet(
						onDismissRequest = onClearArtistSelection,
						artist = artist,
						onPlayNext = onPlayArtistNext,
						onAddToQueue = onAddArtistToQueue,
						onAddAllToPlaylist = { 
							songsToAddToPlaylist = selectedArtistAlbums?.flatMap { it.songs }.orEmpty().toImmutableList()
						},
						onViewOnLastFm = { 
							onClearArtistSelection()
							artist.lastFmUrl?.let { url ->
								uriHandler.openUri(url)
							}
						},
						onViewOnMusicBrainz = { 								
							onClearArtistSelection()
							artist.musicBrainzId?.let { id ->
								uriHandler.openUri(
									"https://musicbrainz.org/artist/$id"
								)
							}
						},
						starred = selectedArtistIsStarred,
						onSetStarred = { onStarSelectedArtist(!selectedArtistIsStarred) }
					)
				}
			}
		}
	}

	songsToAddToPlaylist?.let {
		PlaylistUpdateDialog(
			songs = it,
			onDismissRequest = { songsToAddToPlaylist = null }
		)
	}
}

package paige.navic.ui.screens.song.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_songs
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Note
import paige.navic.ui.components.common.ContentUnavailable

fun LazyListScope.songListScreenContent(
	songs: LazyPagingItems<DomainSong>,
	selectedSong: DomainSong?,
	selectedSongIsStarred: Boolean,
	selectedSongRating: Int,
	allDownloads: List<DownloadEntity>,
	onUpdateSelection: (DomainSong) -> Unit,
	onClearSelection: () -> Unit,
	onSetShareId: (String) -> Unit,
	onSetStarred: (Boolean) -> Unit,
	onPlayNext: (DomainSong) -> Unit,
	onAddToQueue: (DomainSong) -> Unit,
	onPlaySong: (DomainSong) -> Unit,
	onSetRating: (Int) -> Unit,
	onDownload: (DomainSong) -> Unit,
	onCancelDownload: (DomainSong) -> Unit,
	onDeleteDownload: (DomainSong) -> Unit
) {
	if (songs.itemCount > 0) {
		items(count = songs.itemCount, key = { index -> songs[index]?.id ?: index }) { index ->
			val song = songs[index]
			if (song != null) {
				val download = allDownloads.find { it.songId == song.id }
				SongListScreenItem(
					modifier = Modifier.animateItem(),
					song = song,
					selected = song == selectedSong,
					starred = if (song == selectedSong) selectedSongIsStarred else false,
					rating = if (song == selectedSong) selectedSongRating else 0,
					onSelect = { onUpdateSelection(song) },
					onDeselect = { onClearSelection() },
					onSetStarred = { onSetStarred(it) },
					onSetShareId = onSetShareId,
					onPlayNext = { onPlayNext(song) },
					onAddToQueue = { onAddToQueue(song) },
					onClick = { onPlaySong(song) },
					onSetRating = onSetRating,
					download = download,
					onDownload = { onDownload(song) },
					onCancelDownload = { onCancelDownload(song) },
					onDeleteDownload = { onDeleteDownload(song) }
				)
			}
		}
	} else if (songs.loadState.refresh is LoadState.NotLoading) {
		item {
			ContentUnavailable(
				icon = Icons.Outlined.Note,
				label = stringResource(Res.string.info_no_songs)
			)
		}
	}
}

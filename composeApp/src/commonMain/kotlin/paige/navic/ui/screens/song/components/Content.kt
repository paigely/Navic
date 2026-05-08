package paige.navic.ui.screens.song.components

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.ImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_songs
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Note
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.utils.UiState

fun LazyListScope.songListScreenContent(
	state: UiState<ImmutableList<DomainSong>>,
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
	val data = state.data.orEmpty()
	if (data.isNotEmpty()) {
		items(data) { song ->
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
	} else {
		when (state) {
			is UiState.Loading -> {
				// TODO
			}

			else -> {
				item {
					ContentUnavailable(
						icon = Icons.Outlined.Note,
						label = stringResource(Res.string.info_no_songs)
					)
				}
			}
		}
	}
}

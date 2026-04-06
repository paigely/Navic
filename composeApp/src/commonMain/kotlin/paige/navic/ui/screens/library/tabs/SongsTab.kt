package paige.navic.ui.screens.library.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.screens.library.components.LibraryFilterChips
import paige.navic.ui.screens.song.components.songListScreenContent
import paige.navic.utils.UiState
import paige.navic.utils.withoutTop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreenSongsTab(
	scrollBehavior: TopAppBarScrollBehavior,
	innerPadding: PaddingValues,
	songsState: UiState<ImmutableList<DomainSong>>,
	selectedSong: DomainSong?,
	selectedSongIsStarred: Boolean,
	songsListType: DomainSongListType,
	onSelectSong: (DomainSong) -> Unit,
	onClearSongSelection: () -> Unit,
	onStarSelectedSong: (Boolean) -> Unit,
	onSetListType: (DomainSongListType) -> Unit,
	onSetShareId: (String) -> Unit
) {
	val filters = remember { DomainSongListType.entries.toPersistentList() }
	val player = koinViewModel<MediaPlayerViewModel>()

	Column(
		modifier = Modifier
			.nestedScroll(scrollBehavior.nestedScrollConnection)
			.fillMaxSize()
	) {
		LibraryFilterChips(
			items = filters,
			selectedItem = songsListType,
			onItemSelect = onSetListType,
			label = { filter ->
				Text(
					text = stringResource(filter.displayName),
					overflow = TextOverflow.Clip,
					maxLines = 1
				)
			},
			modifier = Modifier.padding(top = 8.dp)
		)

		LazyColumn(
			modifier = Modifier.weight(1f),
			contentPadding = innerPadding.withoutTop()
		) {
			songListScreenContent(
				state = songsState,
				starred = selectedSongIsStarred,
				selectedSong = selectedSong,
				onUpdateSelection = onSelectSong,
				onClearSelection = onClearSongSelection,
				onSetShareId = onSetShareId,
				onSetStarred = onStarSelectedSong,
				onAddToQueue = { song ->
					player.addToQueueSingle(song)
				},
				onPlaySong = { index ->
					val list = songsState.data
					if (!list.isNullOrEmpty()) {
						player.clearQueue()
						list.forEach { song ->
							player.addToQueueSingle(song)
						}
						player.playAt(index)
					}
				}
			)
		}
	}
}

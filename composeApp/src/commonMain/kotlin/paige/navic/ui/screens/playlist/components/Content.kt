package paige.navic.ui.screens.playlist.components

import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.Modifier
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_no_playlists_short
import org.jetbrains.compose.resources.stringResource
import paige.navic.domain.models.DomainPlaylist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.ui.components.common.ContentUnavailable
import paige.navic.ui.components.layouts.artGridPlaceholder
import paige.navic.utils.UiState

fun LazyGridScope.playlistListScreenContent(
	state: UiState<List<DomainPlaylist>>,
	selectedPlaylist: DomainPlaylist?,
	onUpdateSelection: (DomainPlaylist) -> Unit,
	onClearSelection: () -> Unit,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit,
	onAddToQueue: () -> Unit,
) {
	val data = state.data.orEmpty()
	if (data.isNotEmpty()) {
		items(data, { it.id }) { playlist ->
			PlaylistListScreenItem(
				modifier = Modifier.animateItem(),
				tab = "playlists",
				playlist = playlist,
				selected = playlist == selectedPlaylist,
				onSelect = { onUpdateSelection(playlist) },
				onDeselect = { onClearSelection() },
				onSetShareId = onSetShareId,
				onSetDeletionId = onSetDeletionId,
				onAddToQueue = onAddToQueue,
			)
		}
	} else {
		when (state) {
			is UiState.Loading -> {
				artGridPlaceholder()
			}

			else -> {
				item(span = { GridItemSpan(maxLineSpan) }) {
					ContentUnavailable(
						icon = Icons.Outlined.PlaylistRemove,
						label = stringResource(Res.string.info_no_playlists_short)
					)
				}
			}
		}
	}
}

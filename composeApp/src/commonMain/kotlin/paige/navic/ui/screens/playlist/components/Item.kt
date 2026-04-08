package paige.navic.ui.screens.playlist.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.count_songs
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainPlaylist
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.sheets.CollectionSheet
import paige.navic.ui.components.layouts.ArtGridItem

@Composable
fun PlaylistListScreenItem(
	modifier: Modifier = Modifier,
	tab: String,
	playlist: DomainPlaylist,
	selected: Boolean,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetShareId: (String) -> Unit,
	onSetDeletionId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val scope = rememberCoroutineScope()
	Box(modifier) {
		ArtGridItem(
			onClick = {
				ctx.clickSound()
				scope.launch {
					backStack.add(Screen.CollectionDetail(playlist.id, tab))
				}
			},
			onLongClick = onSelect,
			coverArtId = playlist.coverArtId,
			title = playlist.name,
			subtitle = buildString {
				append(
					pluralStringResource(
						Res.plurals.count_songs,
						playlist.songCount,
						playlist.songCount
					)
				)
				playlist.comment?.let {
					append("\n${playlist.comment}\n")
				}
			},
			id = playlist.id,
			tab = tab
		)
		if (selected) {
			CollectionSheet(
				onDismissRequest = onDeselect,
				collection = playlist,
				isOnline = true,
				onShare = { onSetShareId(playlist.id) },
				onDelete = { onSetDeletionId(playlist.id) }
			)
		}
	}
}

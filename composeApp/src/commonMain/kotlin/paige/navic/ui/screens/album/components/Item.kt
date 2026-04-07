package paige.navic.ui.screens.album.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.domain.models.DomainAlbum
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem
import paige.navic.ui.components.layouts.ArtGridItem

@Composable
fun AlbumListScreenItem(
	modifier: Modifier = Modifier,
	tab: String,
	album: DomainAlbum,
	selected: Boolean,
	starred: Boolean,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit,
	onSetShareId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val backStack = LocalNavStack.current
	val scope = rememberCoroutineScope()
	Box(modifier) {
		ArtGridItem(
			onClick = {
				ctx.clickSound()
				scope.launch {
					backStack.add(Screen.TrackList(album.id, tab))
				}
			},
			onLongClick = onSelect,
			coverArtId = album.coverArtId,
			title = album.name,
			subtitle = album.artistName,
			id = album.id,
			tab = tab
		)
		Dropdown(
			expanded = selected,
			onDismissRequest = onDeselect
		) {
			DropdownItem(
				text = { Text(stringResource(Res.string.action_share)) },
				leadingIcon = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					onDeselect()
					onSetShareId(album.id)
				},
			)
			DropdownItem(
				text = {
					Text(
						stringResource(
							if (starred)
								Res.string.action_remove_star
							else Res.string.action_star
						)
					)
				},
				leadingIcon = {
					Icon(if (starred) Icons.Filled.Star else Icons.Outlined.Star, null)
				},
				onClick = {
					onSetStarred(!starred)
					onDeselect()
				}
			)
		}
	}
}

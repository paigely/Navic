package paige.navic.ui.screens.song.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_year
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.DropdownItem

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongListScreenItem(
	modifier: Modifier,
	song: DomainSong,
	selected: Boolean,
	starred: Boolean,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit,
	onSetShareId: (String) -> Unit
) {
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	Box {
		ListItem(
			modifier = modifier,
			onClick = {
				ctx.clickSound()
				player.clearQueue()
				player.addToQueueSingle(song)
				player.playAt(0)
			},
			onLongClick = onSelect,
			content = {
				Text(song.title)
			},
			supportingContent = {
				Text(
					buildString {
						append(song.albumTitle ?: stringResource(Res.string.info_unknown_album))
						append(" • ")
						append(song.artistName)
						append(" • ")
						append(song.year ?: stringResource(Res.string.info_unknown_year))
					},
					maxLines = 1
				)
			},
			leadingContent = {
				CoverArt(
					coverArtId = song.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
				)
			}
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
					onSetShareId(song.id)
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
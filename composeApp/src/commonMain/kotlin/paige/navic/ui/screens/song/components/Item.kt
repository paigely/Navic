package paige.navic.ui.screens.song.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_queue
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_year
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Queue
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.sheets.SongSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.InlineExplicitIcon

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongListScreenItem(
	modifier: Modifier,
	song: DomainSong,
	selected: Boolean,
	starred: Boolean,
	rating: Int,
	onSelect: () -> Unit,
	onDeselect: () -> Unit,
	onSetStarred: (starred: Boolean) -> Unit,
	onSetShareId: (String) -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	onClick: () -> Unit,
	onSetRating: (Int) -> Unit
) {
	val backStack = LocalNavStack.current
	val dismissState = rememberSwipeToDismissBoxState()
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(dismissState.currentValue) {
		if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
			onAddToQueue()
			dismissState.snapTo(SwipeToDismissBoxValue.Settled)
		}
	}

	SwipeToDismissBox(
		modifier = modifier,
		state = dismissState,
		enableDismissFromStartToEnd = false,
		backgroundContent = {
			val backgroundColor by animateColorAsState(
				targetValue = when (dismissState.targetValue) {
					SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.primaryContainer
					else -> Color.Transparent
				}
			)
			val iconColor by animateColorAsState(
				targetValue = when (dismissState.targetValue) {
					SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onPrimaryContainer
					else -> MaterialTheme.colorScheme.onSurfaceVariant
				}
			)

			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(color = backgroundColor)
					.padding(horizontal = 20.dp),
				contentAlignment = Alignment.CenterEnd
			) {
				Icon(
					imageVector = Icons.Outlined.Queue,
					contentDescription = stringResource(Res.string.action_add_to_queue),
					tint = iconColor
				)
			}
		}
	) {
		Box {
			ListItem(
				onClick = onClick,
				onLongClick = onSelect,
				content = {
					MarqueeText(
						text = buildAnnotatedString {
							append(song.title)
							if (song.explicitStatus == DomainExplicitStatus.Explicit) {
								append(" ")
								appendInlineContent("InlineExplicitIcon")
							}
						},
						inlineContent = InlineExplicitIcon,
					)
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
			if (selected) {
				SongSheet(
					onDismissRequest = onDeselect,
					song = song,
					starred = starred,
					rating = rating,
					onSetStarred = onSetStarred,
					onShare = { onSetShareId(song.id) },
					onPlayNext = onPlayNext,
					onAddToQueue = onAddToQueue,
					onTrackInfo = {
						backStack.add(Screen.SongDetail(song.id))
					},
					onViewAlbum = song.albumId?.let { albumId ->
						{
							backStack.add(
								Screen.CollectionDetail(
									collectionId = albumId,
									tab = "library"
								)
							)
						}
					},
					onAddToPlaylist = {
						playlistDialogShown = true
					},
					onSetRating = onSetRating
				)
			}
		}
	}

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = persistentListOf(song),
			onDismissRequest = { playlistDialogShown = false }
		)
	}
}

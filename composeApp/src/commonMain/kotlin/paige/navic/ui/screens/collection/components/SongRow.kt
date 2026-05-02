package paige.navic.ui.screens.collection.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_queue
import navic.composeapp.generated.resources.action_play_next
import navic.composeapp.generated.resources.info_download_failed
import navic.composeapp.generated.resources.info_downloaded
import navic.composeapp.generated.resources.info_not_available_offline
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.DownloadOff
import paige.navic.icons.outlined.Offline
import paige.navic.icons.outlined.Queue
import paige.navic.icons.outlined.QueuePlayNext
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.common.Waveform
import paige.navic.utils.InlineExplicitIcon
import paige.navic.utils.segmentedShapes
import paige.navic.utils.toHoursMinutesSeconds

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionDetailScreenSongRow(
	song: DomainSong,
	index: Int,
	count: Int,
	isPlaylist: Boolean = false,
	onClick: (() -> Unit),
	onLongClick: (() -> Unit),
	onPlayNext: (() -> Unit),
	onAddToQueue: (() -> Unit),
	download: DownloadEntity? = null,
	isOffline: Boolean = false
) {
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()

	val isDownloaded = download?.status == DownloadStatus.DOWNLOADED
	val isCurrentTrack = playerState.currentSong?.id == song.id
	val canPlay = !isOffline || isDownloaded

	val dismissState = rememberSwipeToDismissBoxState()
	val scope = rememberCoroutineScope()

	val itemShape = segmentedShapes(
		index = index,
		count = count,
		dismissDirection = dismissState.dismissDirection
	)

	SwipeToDismissBox(
		modifier = Modifier.padding(horizontal = 16.dp, vertical = 1.5.dp),
		state = dismissState,
		onDismiss = {
			if (it == SwipeToDismissBoxValue.StartToEnd) onPlayNext()
			if (it == SwipeToDismissBoxValue.EndToStart) onAddToQueue()
			scope.launch { dismissState.reset() }
		},
		backgroundContent = {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.clip(MaterialTheme.shapes.largeIncreased)
					.background(MaterialTheme.colorScheme.primaryContainer)
					.padding(horizontal = 20.dp)
			) {
				when (dismissState.dismissDirection) {
					SwipeToDismissBoxValue.StartToEnd -> {
						Icon(
							imageVector = Icons.Outlined.Queue,
							contentDescription = stringResource(Res.string.action_add_to_queue),
							tint = MaterialTheme.colorScheme.onPrimaryContainer,
							modifier = Modifier.align(Alignment.CenterStart)
						)
					}
					SwipeToDismissBoxValue.EndToStart -> {
						Icon(
							imageVector = Icons.Outlined.QueuePlayNext,
							contentDescription = stringResource(Res.string.action_play_next),
							tint = MaterialTheme.colorScheme.onPrimaryContainer,
							modifier = Modifier.align(Alignment.CenterEnd)
						)
					}
					else -> {}
				}
			}
		}
	) {
		SegmentedListItem(
			contentPadding = PaddingValues(14.dp),
			onClick = onClick,
			onLongClick = onLongClick,
			shapes = itemShape,
			colors = ListItemDefaults.segmentedColors(
				containerColor = MaterialTheme.colorScheme.surfaceContainer
			),
			leadingContent = {
				if (isPlaylist) 
						CoverArt(
							modifier = Modifier.size(48.dp),
							coverArtId = song.coverArtId,
							shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
						)
				else 
					Text(
						text = "${index + 1}",
						modifier = Modifier.width(25.dp),
						style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
						fontWeight = FontWeight(400),
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						maxLines = 1,
						textAlign = TextAlign.Center,
						autoSize = TextAutoSize.StepBased(6.sp, 13.sp)
					)
			},
			content = {
				Column {
					MarqueeText(
						text = buildAnnotatedString {
							append(song.title)
							if (song.explicitStatus == DomainExplicitStatus.Explicit) {
								append(" ")
								appendInlineContent("InlineExplicitIcon")
							}
						},
						inlineContent = InlineExplicitIcon
					)
					Text(
						song.artistName,
						style = MaterialTheme.typography.bodySmall,
						maxLines = 1
					)
				}
			},
			trailingContent = {
				Row(verticalAlignment = Alignment.CenterVertically) {
					if (!canPlay) {
						Icon(
							Icons.Outlined.Offline,
							stringResource(Res.string.info_not_available_offline),
							modifier = Modifier.size(20.dp)
						)
						Spacer(Modifier.width(6.dp))
					}
					if (download != null && !isCurrentTrack) {
						when (download.status) {
							DownloadStatus.DOWNLOADING -> {
								CircularProgressIndicator(
									progress = { download.progress },
									modifier = Modifier.size(16.dp),
									strokeWidth = 2.dp
								)
								Spacer(Modifier.width(8.dp))
							}

							DownloadStatus.DOWNLOADED -> {
								Icon(
									Icons.Outlined.Check,
									contentDescription = stringResource(Res.string.info_downloaded),
									modifier = Modifier.size(16.dp),
									tint = MaterialTheme.colorScheme.primary
								)
								Spacer(Modifier.width(8.dp))
							}

							DownloadStatus.FAILED -> {
								Icon(
									Icons.Outlined.DownloadOff,
									contentDescription = stringResource(Res.string.info_download_failed),
									modifier = Modifier.size(16.dp),
									tint = MaterialTheme.colorScheme.error
								)
								Spacer(Modifier.width(8.dp))
							}

							else -> {}
						}
					}
					if (isCurrentTrack) {
						Waveform(
							modifier = Modifier.padding(end = 12.dp),
							isPlaying = !playerState.isPaused
						)
					}
					song.duration.toHoursMinutesSeconds().let {
						Text(
							text = it,
							style = LocalTextStyle.current.copy(fontFeatureSettings = "tnum"),
							fontWeight = FontWeight(400),
							fontSize = 13.sp,
							color = MaterialTheme.colorScheme.onSurfaceVariant,
							maxLines = 1
						)
					}
				}
			}
		)
	}
}

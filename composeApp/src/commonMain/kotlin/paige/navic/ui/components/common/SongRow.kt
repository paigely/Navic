package paige.navic.ui.components.common

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.capsule.ContinuousRoundedRectangle
import kotlinx.collections.immutable.persistentListOf
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_download_failed
import navic.composeapp.generated.resources.info_downloaded
import navic.composeapp.generated.resources.info_not_available_offline
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_year
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalNavStack
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.Screen
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainSong
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.DownloadOff
import paige.navic.icons.outlined.Offline
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.components.dialogs.QueueDuplicateDialog
import paige.navic.ui.components.sheets.SongSheet
import paige.navic.ui.screens.playlist.dialogs.PlaylistUpdateDialog
import paige.navic.utils.InlineExplicitIcon

@Composable
fun SongRow(
	modifier: Modifier = Modifier,
	song: DomainSong,
	selected: Boolean = false,
	onClick: (() -> Unit),
	onLongClick: (() -> Unit),
	isOnline: Boolean = false,
	onDismissRequest: () -> Unit,
	onRemoveStar: () -> Unit,
	onAddStar: () -> Unit,
	onShare: () -> Unit,
	starredState: Boolean,
	download: DownloadEntity? = null,
	onDownload: () -> Unit,
	onCancelDownload: () -> Unit,
	onDeleteDownload: () -> Unit,
	onPlayNext: () -> Unit,
	onAddToQueue: () -> Unit,
	rating: Int,
	onSetRating: (Int) -> Unit
) {
	val player = koinViewModel<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsStateWithLifecycle()

	val backStack = LocalNavStack.current
	var playlistDialogShown by rememberSaveable { mutableStateOf(false) }
	var duplicateQueueDialogShown by rememberSaveable { mutableStateOf(false) }
	var duplicateQueueDialogShownPlayNext by rememberSaveable { mutableStateOf(false) }

	val isDownloaded = download?.status == DownloadStatus.DOWNLOADED
	val isCurrentTrack = playerState.currentSong?.id == song.id
	val canPlay = isOnline || isDownloaded

	ListItem(
		modifier = modifier
			.width(350.dp)
			.combinedClickable (
				onClick = onClick,
				onLongClick = onLongClick
			),
		headlineContent = {
			Text(
				text = buildAnnotatedString {
					append(song.title)
					if (song.explicitStatus == DomainExplicitStatus.Explicit) {
						append(" ")
						appendInlineContent("InlineExplicitIcon")
					}
				},
				inlineContent = InlineExplicitIcon,
				maxLines = 2
			)
		},
		supportingContent = {
			MarqueeText(
				text = buildString {
					append(song.albumTitle ?: stringResource(Res.string.info_unknown_album))
					append(" • ")
					append(song.artistName)
					append(" • ")
					append(song.year ?: stringResource(Res.string.info_unknown_year))
				}
			)
		},
		leadingContent = {
			CoverArt(
				coverArtId = song.coverArtId,
				modifier = Modifier.size(50.dp),
				shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
			)
		},
		trailingContent = {
			Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(83.dp)) {
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
			}
		}
	)

	if (selected) {
		SongSheet(
			onDismissRequest = onDismissRequest,
			song = song,
			starred = starredState,
			rating = rating,
			onSetStarred = { starred ->
				if (starred) onAddStar() else onRemoveStar()
			},
			onShare = onShare,
			onPlayNext = {
				if (player.uiState.value.queue.any { it.id == song.id }) {
					duplicateQueueDialogShown = true
					duplicateQueueDialogShownPlayNext = true
				} else {
					onPlayNext()
				}
			},
			onAddToQueue = {
				if (player.uiState.value.queue.any { it.id == song.id }) {
					duplicateQueueDialogShown = true
					duplicateQueueDialogShownPlayNext = false
				} else {
					onAddToQueue()
				}
			},
			onTrackInfo = {
				backStack.add(Screen.SongDetail(song.id))
			},
			onViewAlbum = {
				backStack.add(
					Screen.CollectionDetail(
						collectionId = song.albumId as String,
						tab = "library"
					)
				)
			},
			onAddToPlaylist = {
				playlistDialogShown = true
			},
			downloadStatus = download?.status,
			isOnline = isOnline,
			onDownload = onDownload,
			onCancelDownload = onCancelDownload,
			onDeleteDownload = onDeleteDownload,
			onSetRating = onSetRating
		)
	}

	if (playlistDialogShown) {
		@Suppress("AssignedValueIsNeverRead")
		PlaylistUpdateDialog(
			songs = persistentListOf(song),
			onDismissRequest = { playlistDialogShown = false }
		)
	}

	if (duplicateQueueDialogShown) {
		QueueDuplicateDialog(
			onDismissRequest = {
				duplicateQueueDialogShown = false
				onDismissRequest()
			},
			onConfirm = {
				if (duplicateQueueDialogShownPlayNext) onPlayNext() else onAddToQueue()
			}
		)
	}
}

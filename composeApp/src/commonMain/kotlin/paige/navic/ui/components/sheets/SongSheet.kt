package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_add_to_another_playlist
import navic.composeapp.generated.resources.action_add_to_playlist
import navic.composeapp.generated.resources.action_add_to_queue
import navic.composeapp.generated.resources.action_cancel_download
import navic.composeapp.generated.resources.action_delete_download
import navic.composeapp.generated.resources.action_download
import navic.composeapp.generated.resources.action_play_next
import navic.composeapp.generated.resources.action_remove_from_playlist
import navic.composeapp.generated.resources.action_remove_star
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.action_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer_enabled
import navic.composeapp.generated.resources.action_star
import navic.composeapp.generated.resources.action_track_info
import navic.composeapp.generated.resources.action_view_album
import navic.composeapp.generated.resources.action_view_artist
import navic.composeapp.generated.resources.info_click_to_retry
import navic.composeapp.generated.resources.info_download_failed
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalCtx
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.filled.Star
import paige.navic.icons.outlined.Album
import paige.navic.icons.outlined.Artist
import paige.navic.icons.outlined.Bedtime
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.DownloadOff
import paige.navic.icons.outlined.Info
import paige.navic.icons.outlined.PlaylistAdd
import paige.navic.icons.outlined.PlaylistRemove
import paige.navic.icons.outlined.Queue
import paige.navic.icons.outlined.QueuePlayNext
import paige.navic.icons.outlined.Share
import paige.navic.icons.outlined.Star
import paige.navic.managers.SleepTimerManager
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.MarqueeText
import paige.navic.ui.components.common.RatingRow
import paige.navic.ui.theme.positive
import paige.navic.utils.InlineExplicitIcon
import paige.navic.utils.label

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SongSheet(
	onDismissRequest: () -> Unit,
	song: DomainSong,
	collection: DomainSongCollection? = null,
	starred: Boolean? = null,
	onSetStarred: ((Boolean) -> Unit)? = null,
	onShare: (() -> Unit)? = null,
	onPlayNext: (() -> Unit)? = null,
	onAddToQueue: (() -> Unit)? = null,
	onTrackInfo: (() -> Unit)? = null,
	onViewAlbum: (() -> Unit)? = null,
	onViewArtist: (() -> Unit)? = null,
	onAddToPlaylist: (() -> Unit)? = null,
	onRemoveFromPlaylist: (() -> Unit)? = null,
	downloadStatus: DownloadStatus? = null,
	onDownload: (() -> Unit)? = null,
	onCancelDownload: (() -> Unit)? = null,
	onDeleteDownload: (() -> Unit)? = null,
	rating: Int? = null,
	onSetRating: ((Int) -> Unit)? = null,
	showSleepTimer: Boolean = false
) {
	val ctx = LocalCtx.current
	var sleepTimerSheetShown by rememberSaveable { mutableStateOf(false) }
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val sleepTimerLeft = sleepTimerManager.timeLeft
	val contentPadding = PaddingValues(horizontal = 16.dp)
	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		dragHandle = null,
		sheetState = rememberModalBottomSheetState(true),
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					left = 8.dp,
					right = 8.dp
				)
			)
		}
	) {
		Spacer(Modifier.height(16.dp))

		ListItem(
			headlineContent = {
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
				MarqueeText(
					"${song.albumTitle ?: ""} • ${song.artistName} • ${song.year ?: ""}"
				)
			},
			leadingContent = {
				CoverArt(
					coverArtId = song.coverArtId,
					modifier = Modifier.size(50.dp),
					shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
				)
			},
			colors = colors
		)
		if (rating != null && onSetRating != null) {
			RatingRow(
				rating = rating,
				setRating = onSetRating
			)
			Spacer(Modifier.height(14.dp))
		}

		HorizontalDivider(Modifier.padding(horizontal = 8.dp, vertical = 2.dp))

		if (onShare != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_share)) },
				leadingContent = { Icon(Icons.Outlined.Share, null) },
				onClick = {
					ctx.clickSound()
					onShare()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (starred != null && onSetStarred != null) {
			ListItem(
				content = {
					Text(stringResource(if (starred) Res.string.action_remove_star else Res.string.action_star))
				},
				leadingContent = {
					Icon(if (starred) Icons.Filled.Star else Icons.Outlined.Star, null)
				},
				onClick = {
					ctx.clickSound()
					onSetStarred(!starred)
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (downloadStatus != null) {
			when (downloadStatus) {
				DownloadStatus.DOWNLOADING -> {
					ListItem(
						content = { Text(stringResource(Res.string.action_cancel_download)) },
						leadingContent = { Icon(Icons.Outlined.Close, null) },
						onClick = {
							ctx.clickSound()
							onCancelDownload?.invoke()
							onDismissRequest()
						},
						colors = colors,
						contentPadding = contentPadding
					)
				}

				DownloadStatus.DOWNLOADED -> {
					ListItem(
						content = { Text(stringResource(Res.string.action_delete_download)) },
						leadingContent = { Icon(Icons.Outlined.Delete, null) },
						onClick = {
							ctx.clickSound()
							onDeleteDownload?.invoke()
							onDismissRequest()
						},
						colors = colors,
						contentPadding = contentPadding
					)
				}

				DownloadStatus.FAILED -> {
					ListItem(
						content = {
							Text(
								text = stringResource(Res.string.info_download_failed),
								color = MaterialTheme.colorScheme.error
							)
						},
						supportingContent = {
							Text(
								text = stringResource(Res.string.info_click_to_retry),
								color = MaterialTheme.colorScheme.error,
								style = MaterialTheme.typography.labelSmall
							)
						},
						leadingContent = {
							Icon(
								Icons.Outlined.DownloadOff,
								null,
								tint = MaterialTheme.colorScheme.error
							)
						},
						onClick = {
							ctx.clickSound()
							onDownload?.invoke()
							onDismissRequest()
						},
						colors = colors,
						contentPadding = contentPadding
					)
				}

				else -> {
					ListItem(
						content = { Text(stringResource(Res.string.action_download)) },
						leadingContent = { Icon(Icons.Outlined.Download, null) },
						onClick = {
							ctx.clickSound()
							onDownload?.invoke()
							onDismissRequest()
						},
						colors = colors,
						contentPadding = contentPadding
					)
				}
			}
		} else if (onDownload != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_download)) },
				leadingContent = { Icon(Icons.Outlined.Download, null) },
				onClick = {
					ctx.clickSound()
					onDownload()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onPlayNext != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_play_next)) },
				leadingContent = { Icon(Icons.Outlined.QueuePlayNext, null) },
				onClick = {
					ctx.clickSound()
					onPlayNext()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onAddToQueue != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_add_to_queue)) },
				leadingContent = { Icon(Icons.Outlined.Queue, null) },
				onClick = {
					ctx.clickSound()
					onAddToQueue()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onAddToPlaylist != null) {
			ListItem(
				content = {
					Text(
						stringResource(
							if (collection != null && collection !is DomainAlbum)
								Res.string.action_add_to_another_playlist
							else Res.string.action_add_to_playlist
						)
					)
				},
				leadingContent = { Icon(Icons.Outlined.PlaylistAdd, null) },
				onClick = {
					ctx.clickSound()
					onAddToPlaylist()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onRemoveFromPlaylist != null && collection != null && collection !is DomainAlbum) {
			ListItem(
				content = { Text(stringResource(Res.string.action_remove_from_playlist)) },
				leadingContent = { Icon(Icons.Outlined.PlaylistRemove, null) },
				onClick = {
					ctx.clickSound()
					onRemoveFromPlaylist()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onViewAlbum != null) {
			ListItem(
				content = {
					Text(stringResource(Res.string.action_view_album))
				},
				leadingContent = { Icon(Icons.Outlined.Album, null) },
				onClick = {
					ctx.clickSound()
					onViewAlbum()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (onViewArtist != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_view_artist)) },
				leadingContent = { Icon(Icons.Outlined.Artist, null) },
				onClick = {
					ctx.clickSound()
					onViewArtist()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}

		if (showSleepTimer) {
			if (sleepTimerLeft != null) {
				ListItem(
					content = {
						Text(
							stringResource(
								Res.string.action_sleep_timer_enabled,
								sleepTimerLeft.label()
							),
							color = MaterialTheme.colorScheme.positive
						)
					},
					leadingContent = {
						Icon(
							Icons.Outlined.Bedtime,
							null,
							tint = MaterialTheme.colorScheme.positive
						)
					},
					onClick = {
						ctx.clickSound()
						sleepTimerSheetShown = true
					},
					colors = colors,
					contentPadding = contentPadding
				)
			} else {
				ListItem(
					content = {
						Text(
							stringResource(Res.string.action_sleep_timer)
						)
					},
					leadingContent = {
						Icon(
							Icons.Outlined.Bedtime,
							null
						)
					},
					onClick = {
						ctx.clickSound()
						sleepTimerSheetShown = true
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}
		}

		if (onTrackInfo != null) {
			ListItem(
				content = { Text(stringResource(Res.string.action_track_info)) },
				leadingContent = { Icon(Icons.Outlined.Info, null) },
				onClick = {
					ctx.clickSound()
					onTrackInfo()
					onDismissRequest()
				},
				colors = colors,
				contentPadding = contentPadding
			)
		}
	}

	if (sleepTimerSheetShown) {
		SleepTimerSheet(
			onDismissRequest = { confirmed ->
				sleepTimerSheetShown = false
				if (confirmed) {
					onDismissRequest()
				}
			}
		)
	}
}

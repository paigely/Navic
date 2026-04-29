package paige.navic.ui.screens.artist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel_download
import navic.composeapp.generated.resources.action_delete_download
import navic.composeapp.generated.resources.action_play
import navic.composeapp.generated.resources.info_download_failed
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.icons.Icons
import paige.navic.icons.filled.Play
import paige.navic.icons.outlined.Close
import paige.navic.icons.outlined.Delete
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.DownloadOff

@Composable
fun ArtistActionButtons(
	onPlay: () -> Unit,
	onDownload: () -> Unit,
	onCancelDownload: () -> Unit,
	onDeleteDownload: () -> Unit,
	downloadStatus: DownloadStatus,
	playEnabled: Boolean,
	modifier: Modifier = Modifier
) {
	val ctx = LocalCtx.current

	Row(
		modifier = modifier
			.fillMaxWidth()
			.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.weight(1f)
				.height(52.dp)
				.clip(ContinuousCapsule)
				.background(
					if (playEnabled) MaterialTheme.colorScheme.primary
					else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
				)
				.clickable(enabled = playEnabled) {
					ctx.clickSound()
					onPlay()
				},
			contentAlignment = Alignment.Center
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				horizontalArrangement = Arrangement.spacedBy(8.dp)
			) {
				Icon(Icons.Filled.Play, null, tint = MaterialTheme.colorScheme.onPrimary)
				Text(
					stringResource(Res.string.action_play),
					style = MaterialTheme.typography.labelLarge,
					color = MaterialTheme.colorScheme.onPrimary,
					fontWeight = FontWeight.Bold
				)
			}
		}

		OutlinedButton(
			modifier = Modifier.size(width = 52.dp, height = 40.dp),
			onClick = {
				ctx.clickSound()
				when (downloadStatus) {
					DownloadStatus.NOT_DOWNLOADED, DownloadStatus.FAILED -> onDownload()
					DownloadStatus.DOWNLOADING -> onCancelDownload()
					DownloadStatus.DOWNLOADED -> onDeleteDownload()
				}
			},
			shape = ContinuousCapsule,
			enabled = when (downloadStatus) {
				DownloadStatus.DOWNLOADING,
				DownloadStatus.DOWNLOADED,
				DownloadStatus.FAILED -> true

				DownloadStatus.NOT_DOWNLOADED -> playEnabled
			},
			contentPadding = PaddingValues(0.dp)
		) {
			when (downloadStatus) {
				DownloadStatus.DOWNLOADING -> {
					Box(contentAlignment = Alignment.Center) {
						CircularProgressIndicator(
							modifier = Modifier.size(24.dp),
							strokeWidth = 2.5.dp,
							color = MaterialTheme.colorScheme.primary
						)
						Icon(
							imageVector = Icons.Outlined.Close,
							contentDescription = stringResource(Res.string.action_cancel_download),
							modifier = Modifier.size(12.dp),
							tint = MaterialTheme.colorScheme.primary
						)
					}
				}

				DownloadStatus.DOWNLOADED -> {
					Icon(
						imageVector = Icons.Outlined.Delete,
						contentDescription = stringResource(Res.string.action_delete_download),
						modifier = Modifier.size(24.dp),
						tint = MaterialTheme.colorScheme.primary
					)
				}

				DownloadStatus.FAILED -> {
					Icon(
						imageVector = Icons.Outlined.DownloadOff,
						contentDescription = stringResource(Res.string.info_download_failed),
						modifier = Modifier.size(24.dp),
						tint = MaterialTheme.colorScheme.error
					)
				}

				else -> {
					Icon(
						imageVector = Icons.Outlined.Download,
						contentDescription = null,
						modifier = Modifier.size(24.dp)
					)
				}
			}
		}
	}
}

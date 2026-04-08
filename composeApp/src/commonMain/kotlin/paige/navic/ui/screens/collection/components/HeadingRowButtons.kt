package paige.navic.ui.screens.collection.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousCapsule
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_play
import navic.composeapp.generated.resources.action_shuffle
import navic.composeapp.generated.resources.info_download_failed
import navic.composeapp.generated.resources.info_downloaded
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainSongCollection
import paige.navic.icons.Icons
import paige.navic.icons.filled.Play
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Download
import paige.navic.icons.outlined.DownloadOff
import paige.navic.icons.outlined.Shuffle
import paige.navic.managers.DownloadManager
import paige.navic.shared.MediaPlayerViewModel
import paige.navic.ui.theme.defaultFont

@Composable
fun CollectionDetailScreenHeadingRowButtons(
	collection: DomainSongCollection,
	isOnline: Boolean
) {
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	val downloadManager = koinInject<DownloadManager>()
	val scope = rememberCoroutineScope()

	val downloadStatus by downloadManager
		.getCollectionDownloadStatus(collection.songs.map { it.id })
		.collectAsState(initial = DownloadStatus.NOT_DOWNLOADED)

	Row(
		modifier = Modifier.padding(horizontal = 31.dp, vertical = 10.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(
			10.dp,
			alignment = Alignment.CenterHorizontally
		)
	) {
		val shape = ContinuousCapsule
		Button(
			modifier = Modifier.weight(1f),
			onClick = {
				ctx.clickSound()
				player.clearQueue()
				player.addToQueue(collection)
				player.playAt(0)
			},
			shape = shape
		) {
			Icon(
				Icons.Filled.Play,
				null,
				modifier = Modifier.size(24.dp).padding(end = 4.dp)
			)
			Text(
				stringResource(Res.string.action_play),
				maxLines = 1,
				autoSize = TextAutoSize.StepBased(
					minFontSize = 1.sp,
					maxFontSize = MaterialTheme.typography.labelLarge.fontSize
				),
				fontFamily = defaultFont(grade = 100)
			)
		}
		OutlinedButton(
			modifier = Modifier.weight(1f),
			onClick = {
				ctx.clickSound()
				player.shufflePlay(collection)
			},
			shape = shape
		) {
			Icon(
				Icons.Outlined.Shuffle,
				null,
				modifier = Modifier.size(22.dp).padding(end = 6.dp)
			)
			Text(
				stringResource(Res.string.action_shuffle),
				maxLines = 1,
				autoSize = TextAutoSize.StepBased(
					minFontSize = 1.sp,
					maxFontSize = MaterialTheme.typography.labelLarge.fontSize
				),
				fontFamily = defaultFont(grade = 100)
			)
		}
		OutlinedButton(
			modifier = Modifier.size(width = 52.dp, height = 40.dp),
			onClick = {
				ctx.clickSound()
				if (downloadStatus == DownloadStatus.NOT_DOWNLOADED
					|| downloadStatus == DownloadStatus.FAILED
				) {
					scope.launch {
						downloadManager.downloadCollection(collection)
					}
				}
			},
			shape = shape,
			enabled = downloadStatus == DownloadStatus.NOT_DOWNLOADED
				&& collection.songs.isNotEmpty()
				&& isOnline
				|| downloadStatus == DownloadStatus.FAILED,
			contentPadding = PaddingValues(0.dp)
		) {
			when (downloadStatus) {
				DownloadStatus.DOWNLOADING -> {
					CircularProgressIndicator(
						modifier = Modifier.size(24.dp),
						strokeWidth = 2.5.dp,
						color = MaterialTheme.colorScheme.primary
					)
				}

				DownloadStatus.DOWNLOADED -> {
					Icon(
						imageVector = Icons.Outlined.Check,
						contentDescription = stringResource(Res.string.info_downloaded),
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

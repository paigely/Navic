package paige.navic.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_year
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.domain.models.DomainSong
import paige.navic.shared.MediaPlayerViewModel

@Composable
fun TrackRow(
	modifier: Modifier = Modifier,
	track: DomainSong
) {
	val ctx = LocalCtx.current
	val player = koinViewModel<MediaPlayerViewModel>()
	ListItem(
		modifier = modifier.clickable {
			ctx.clickSound()
			player.clearQueue()
			player.addToQueueSingle(track)
			player.playAt(0)
		},
		headlineContent = {
			Text(track.title)
		},
		supportingContent = {
			Text(
				buildString {
					append(track.albumTitle ?: stringResource(Res.string.info_unknown_album))
					append(" • ")
					append(track.artistName)
					append(" • ")
					append(track.year ?: stringResource(Res.string.info_unknown_year))
				},
				maxLines = 1
			)
		},
		leadingContent = {
			CoverArt(
				coverArtId = track.coverArtId,
				modifier = Modifier.size(50.dp),
				shape = ContinuousRoundedRectangle((Settings.shared.artGridRounding / 1.75f).dp)
			)
		}
	)
}
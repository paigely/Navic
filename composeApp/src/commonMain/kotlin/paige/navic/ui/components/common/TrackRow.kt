package paige.navic.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_artist
import navic.composeapp.generated.resources.info_unknown_year
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.subsonic.api.models.Track

@Composable
fun TrackRow(
	modifier: Modifier = Modifier,
	track: Track
) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	ListItem(
		modifier = modifier.clickable {
			ctx.clickSound()
			player.playSingle(track)
		},
		headlineContent = {
			Text(track.title)
		},
		supportingContent = {
			Text(
				buildString {
					append(track.album ?: stringResource(Res.string.info_unknown_album))
					append(" • ")
					append(track.artist ?: stringResource(Res.string.info_unknown_artist))
					append(" • ")
					append(track.year ?: stringResource(Res.string.info_unknown_year))
				},
				maxLines = 1
			)
		},
		leadingContent = {
			AsyncImage(
				model = track.coverArt,
				contentDescription = null,
				modifier = Modifier
					.padding(start = 6.5.dp)
					.size(50.dp)
					.clip(ContinuousRoundedRectangle(8.dp)),
				contentScale = ContentScale.Crop
			)
		}
	)
}
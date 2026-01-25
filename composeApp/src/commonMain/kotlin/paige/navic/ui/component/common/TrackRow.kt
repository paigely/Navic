package paige.navic.ui.component.common

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
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.subsonic.api.model.Track

@Composable
fun TrackRow(
	modifier: Modifier = Modifier,
	track: Track,
	onClick: () -> Unit = {}
) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	ListItem(
		modifier = modifier.clickable {
			ctx.clickSound()
			player.playSingle(track)
			onClick()
		},
		headlineContent = {
			Text(track.title)
		},
		supportingContent = {
			Text(
				buildString {
					append(track.album ?: "Unknown album")
					append(" • ")
					append(track.artist ?: "Unknown artist(s)")
					append(" • ")
					append(track.year ?: "Unknown year")
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
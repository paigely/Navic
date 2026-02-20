package paige.navic.ui.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown_album
import navic.composeapp.generated.resources.info_unknown_artist
import navic.composeapp.generated.resources.info_unknown_year
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalMediaPlayer
import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Track

@Composable
fun TrackRow(
	modifier: Modifier = Modifier,
	track: Track
) {
	val ctx = LocalCtx.current
	val player = LocalMediaPlayer.current
	val platformContext = LocalPlatformContext.current
	val model = remember(track.coverArt) {
		ImageRequest.Builder(platformContext)
			.data(SessionManager.api.getCoverArtUrl(track.coverArt, auth = true))
			.memoryCacheKey(track.coverArt)
			.diskCacheKey(track.coverArt)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.crossfade(500)
			.build()
	}
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
				model = model,
				contentDescription = null,
				modifier = Modifier
					.padding(start = 6.5.dp)
					.size(50.dp)
					.clip(MaterialTheme.shapes.small),
				contentScale = ContentScale.Crop
			)
		}
	)
}
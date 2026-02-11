package paige.navic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import paige.navic.data.session.SessionManager

@Composable
fun rememberTrackPainter(trackId: String?, coverArt: String?): Painter {
	val context = LocalPlatformContext.current

	val coverUri = remember(coverArt) {
		SessionManager.api.getCoverArtUrl(coverArt, auth = true)
	}

	val imageRequest = remember(coverUri, trackId) {
		ImageRequest.Builder(context)
			.data(coverUri)
			.crossfade(500)
			.memoryCacheKey(trackId)
			.diskCacheKey(trackId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}

	return rememberAsyncImagePainter(imageRequest)
}
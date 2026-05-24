package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_image_failed_to_load
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Error
import paige.navic.util.core.Logger
import paige.navic.ui.theme.defaultFont
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

@Composable
fun CoverArt(
	modifier: Modifier = Modifier,
	coverArtId: String?,
	contentDescription: String? = null,
	onClick: (() -> Unit)? = null,
	onLongClick: (() -> Unit)? = null,
	square: Boolean = true,
	crossfadeMs: Int = 500,
	shadowElevation: Dp = 0.dp,
	interactionSource: MutableInteractionSource? = null,
	shape: Shape? = null
) {
	val preferenceManager = koinInject<PreferenceManager>()
	val shape = shape ?: preferenceManager.coverArtShape.shape
	val coilPlatformContext = LocalCoilPlatformContext.current
	val customHeaders = preferenceManager.customHeaders
	val sessionManager = koinInject<SessionManager>()
	val model = remember(coverArtId, customHeaders) {
		val networkHeaders = NetworkHeaders.Builder().apply {
			preferenceManager.customHeadersMap().forEach { (key, value) -> add(key, value) }
		}.build()
		ImageRequest.Builder(coilPlatformContext)
			.data(coverArtId?.let { sessionManager.getCoverArtUrl(it) })
			.memoryCacheKey(coverArtId)
			.diskCacheKey(coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.crossfade(crossfadeMs)
			.httpHeaders(networkHeaders)
			.build()
	}

	val commonModifier = modifier
		.then(if (square) Modifier.aspectRatio(1f) else Modifier)
		.shadow(shadowElevation, shape)
		.clip(shape)
		.background(MaterialTheme.colorScheme.surfaceContainer)
		.then(if (onClick != null)
			Modifier.combinedClickable(
				onClick = onClick,
				onLongClick = onLongClick,
				interactionSource = interactionSource
			)
		else Modifier)
		.then(if (interactionSource != null)
			Modifier.indication(interactionSource, ripple())
		else Modifier)

	if (coverArtId.isNullOrBlank()) return Box(commonModifier)
	SubcomposeAsyncImage(
		model = model,
		contentDescription = contentDescription,
		modifier = commonModifier,
		contentScale = ContentScale.Crop,
		error = {
			LaunchedEffect(it.result.throwable) {
				Logger.w(
					"CoverArt",
					"Failed to load cover art, falling back to placeholder",
					it.result.throwable
				)
			}
			LazyColumn(
				horizontalAlignment = Alignment.CenterHorizontally,
				verticalArrangement = Arrangement.Center
			) {
				item { Icon(Icons.Outlined.Error, null) }
				item {
					Text(
						stringResource(Res.string.info_image_failed_to_load),
						maxLines = 1,
						autoSize = TextAutoSize.StepBased(
							minFontSize = 1.sp,
							maxFontSize = 14.sp
						),
						fontFamily = defaultFont(grade = 10, round = 100f)
					)
				}
			}
		}
	)
}

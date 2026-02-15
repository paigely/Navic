package paige.navic.ui.components.layouts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.CarouselDefaults
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalImageBuilder

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ArtCarousel(
	title: StringResource,
	items: List<T>,
	content: @Composable CarouselItemScope.(item: T) -> Unit
) {
	if (items.isNotEmpty()) {
		val state = rememberCarouselState { items.count() }
		Column(Modifier.padding(horizontal = 20.dp)) {
			Text(
				stringResource(title),
				style = MaterialTheme.typography.titleMediumEmphasized,
				fontWeight = FontWeight(600),
				modifier = Modifier.heightIn(min = 32.dp).padding(top = 8.dp)
			)
			HorizontalMultiBrowseCarousel(
				state = state,
				flingBehavior = CarouselDefaults.multiBrowseFlingBehavior(
					state = state
				),
				modifier = Modifier
					.fillMaxWidth()
					.wrapContentHeight()
					.padding(top = 16.dp, bottom = 16.dp),
				preferredItemWidth = 150.dp,
				itemSpacing = 8.dp
			) { index ->
				content(items[index])
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarouselItemScope.ArtCarouselItem(
	image: String?,
	contentDescription: String?,
	onClick: () -> Unit = {}
) {
	val ctx = LocalCtx.current
	val focusManager = LocalFocusManager.current
	val imageBuilder = LocalImageBuilder.current
	AsyncImage(
		model = imageBuilder
			.data(image)
			.memoryCacheKey(image.toString())
			.diskCacheKey(image.toString())
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build(),
		contentDescription = contentDescription,
		modifier = Modifier
			.fillMaxWidth()
			.aspectRatio(1f)
			.maskClip(MaterialTheme.shapes.large)
			.clickable {
				ctx.clickSound()
				focusManager.clearFocus(true)
				onClick()
			},
		contentScale = ContentScale.Crop
	)
}

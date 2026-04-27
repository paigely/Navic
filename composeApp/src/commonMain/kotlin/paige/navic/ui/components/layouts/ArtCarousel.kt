package paige.navic.ui.components.layouts

import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import paige.navic.LocalCtx
import paige.navic.ui.components.common.CoverArt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> ArtCarousel(
	title: String,
	items: ImmutableList<T>,
	content: @Composable CarouselItemScope.(item: T) -> Unit
) {
	if (items.isNotEmpty()) {
		val state = rememberCarouselState { items.count() }
		Column(Modifier.padding(horizontal = 16.dp)) {
			Text(
				title,
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
	coverArtId: String?,
	title: String,
	contentDescription: String?,
	onSelect: () -> Unit = {},
	onClick: () -> Unit = {}
) {
	val ctx = LocalCtx.current
	val focusManager = LocalFocusManager.current

	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		CoverArt(
			coverArtId = coverArtId,
			contentDescription = contentDescription,
			modifier = Modifier
				.fillMaxWidth()
				.maskClip(MaterialTheme.shapes.large),
			shape = RectangleShape,
			onClick = {
				ctx.clickSound()
				focusManager.clearFocus(true)
				onClick()
			},
			onLongClick = onSelect
		)

		Text(
			text = title,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Medium,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis,
			modifier = Modifier
				.padding(top = 8.dp, start = 4.dp, end = 4.dp)
		)
	}
}

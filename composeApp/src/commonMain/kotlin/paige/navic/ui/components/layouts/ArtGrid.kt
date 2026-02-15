package paige.navic.ui.components.layouts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.LocalImageBuilder
import paige.navic.data.models.Settings
import paige.navic.ui.components.common.ErrorBox
import paige.navic.utils.UiState
import paige.navic.utils.shimmerLoading

@Composable
fun ArtGrid(
	modifier: Modifier = Modifier,
	state: LazyGridState = rememberLazyGridState(),
	content: LazyGridScope.() -> Unit
) {
	val ctx = LocalCtx.current
	val artGridItemSize = Settings.shared.artGridItemSize
	LazyVerticalGrid(
		modifier = modifier.fillMaxSize(),
		state = state,
		columns = if (ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact)
			GridCells.Fixed(Settings.shared.gridSize.value)
		else GridCells.Adaptive(artGridItemSize.dp),
		contentPadding = PaddingValues(
			start = 16.dp,
			top = 16.dp,
			end = 16.dp,
			bottom = LocalContentPadding.current.calculateBottomPadding(),
		),
		verticalArrangement = Arrangement.spacedBy(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		content = content
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtGridItem(
	imageModifier: Modifier = Modifier,
	imageUrl: String?,
	title: String,
	subtitle: String? = null
) {
	val imageBuilder = LocalImageBuilder.current
	val artGridRounding = Settings.shared.artGridRounding
	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		AsyncImage(
			model = imageBuilder
				.data(imageUrl)
				.memoryCacheKey(imageUrl)
				.diskCacheKey(imageUrl)
				.diskCachePolicy(CachePolicy.ENABLED)
				.memoryCachePolicy(CachePolicy.ENABLED)
				.build(),
			contentDescription = title,
			contentScale = ContentScale.Crop,
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
				.clip(
					ContinuousRoundedRectangle(artGridRounding.dp)
				)
				.background(MaterialTheme.colorScheme.surfaceContainer)
				.then(imageModifier)
		)
		Text(
			text = title,
			style = MaterialTheme.typography.titleSmallEmphasized,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 6.dp)
		)
		subtitle?.let {
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				modifier = Modifier.fillMaxWidth(),
				maxLines = 2
			)
		}
	}
}

@Composable
fun ArtGridPlaceholder(
	modifier: Modifier = Modifier
) {
	Column(modifier = modifier) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.aspectRatio(1f)
				.clip(MaterialTheme.shapes.large)
				.shimmerLoading()
		)
		Box(
			modifier = Modifier
				.padding(top = 6.dp)
				.fillMaxWidth(0.8f)
				.height(16.dp)
				.clip(ContinuousCapsule)
				.shimmerLoading()
		)
		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.fillMaxWidth(0.6f)
				.height(14.dp)
				.clip(ContinuousCapsule)
				.shimmerLoading()
		)
	}
}
fun LazyGridScope.artGridPlaceholder(
	itemCount: Int = 8
) {
	items(itemCount) {
		ArtGridPlaceholder(Modifier.fillMaxWidth())
	}
}

fun LazyGridScope.artGridError(
	state: UiState.Error
) {
	item(span = { GridItemSpan(maxCurrentLineSpan) }) {
		ErrorBox(
			modifier = Modifier.animateItem(),
			error = state
		)
	}
}

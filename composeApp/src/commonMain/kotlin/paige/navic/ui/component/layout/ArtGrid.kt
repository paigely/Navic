package paige.navic.ui.component.layout

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
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.burnoo.compose.remembersetting.rememberFloatSetting
import paige.navic.LocalImageBuilder
import paige.navic.util.shimmerLoading

@Composable
fun ArtGrid(
	modifier: Modifier = Modifier,
	content: LazyGridScope.() -> Unit
) {
	var artGridSize by rememberFloatSetting("artGridSize", 150f)
	LazyVerticalGrid(
		modifier = modifier.fillMaxSize(),
		columns = GridCells.Adaptive(artGridSize.dp),
		contentPadding = PaddingValues(
			start = 16.dp,
			top = 16.dp,
			end = 16.dp,
			bottom = 200.dp,
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
	subtitle: String
) {
	val imageBuilder = LocalImageBuilder.current
	var artGridRounding by rememberFloatSetting("artGridRounding", 16f)
	Column(
		modifier = Modifier
			.fillMaxWidth()
	) {
		AsyncImage(
			model = imageBuilder
				.data(imageUrl)
				.memoryCacheKey(imageUrl)
				.diskCacheKey(imageUrl)
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
			style = MaterialTheme.typography.bodyMedium,
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 6.dp)
		)
		Text(
			text = subtitle,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			modifier = Modifier.fillMaxWidth()
		)
	}
}

@Composable
fun ArtGridPlaceholder(
	modifier: Modifier = Modifier,
	itemCount: Int = 8
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(150.dp),
		modifier = modifier.fillMaxSize(),
		userScrollEnabled = false,
		contentPadding = PaddingValues(
			start = 16.dp,
			top = 16.dp,
			end = 16.dp,
			bottom = 200.dp,
		),
		verticalArrangement = Arrangement.spacedBy(12.dp),
		horizontalArrangement = Arrangement.spacedBy(12.dp),
	) {
		items(itemCount) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
			) {
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.aspectRatio(1f)
						.clip(ContinuousRoundedRectangle(16.dp))
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
	}
}

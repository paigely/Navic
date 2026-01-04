package paige.navic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import paige.navic.LocalImageBuilder

@Composable
fun ArtGrid(
	modifier: Modifier = Modifier,
	content: LazyGridScope.() -> Unit
) {
	LazyVerticalGrid(
		columns = GridCells.Adaptive(150.dp),
		modifier = modifier.fillMaxSize(),
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
	modifier: Modifier = Modifier,
	imageUrl: String?,
	title: String,
	subtitle: String
) {
	val imageBuilder = LocalImageBuilder.current
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
	Column(
		modifier = Modifier
			.clip(
				ContinuousRoundedRectangle(
					if (roundCoverArt) 16.dp else 0.dp
				)
			)
			.fillMaxWidth()
			.then(modifier)
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
					ContinuousRoundedRectangle(
						if (roundCoverArt) 16.dp else 0.dp
					)
				)
				.background(MaterialTheme.colorScheme.surfaceContainer)
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
fun ArtGridPlaceholder() {
	val scrollState = rememberScrollState()
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState)
			.padding(14.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			"Nobody here but us chickens!",
			color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
			fontStyle = FontStyle.Italic
		)
	}
}

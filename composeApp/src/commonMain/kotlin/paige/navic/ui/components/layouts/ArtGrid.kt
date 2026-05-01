package paige.navic.ui.components.layouts

import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.core.tween
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import paige.navic.LocalCtx
import paige.navic.LocalSharedTransitionScope
import paige.navic.data.models.settings.Settings
import paige.navic.ui.components.common.CoverArt
import paige.navic.ui.components.common.ErrorBox
import paige.navic.utils.EmphasizedDecelerateEasing
import paige.navic.utils.UiState
import paige.navic.utils.shimmerLoading

@Composable
fun ArtGrid(
	modifier: Modifier = Modifier,
	state: LazyGridState = rememberLazyGridState(),
	contentPadding: PaddingValues,
	horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
	verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(12.dp),
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
		contentPadding = contentPadding + PaddingValues(
			start = 16.dp,
			top = 16.dp,
			end = 16.dp
		),
		horizontalArrangement = horizontalArrangement,
		verticalArrangement = verticalArrangement,
		content = content
	)
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ArtGridItem(
	modifier: Modifier = Modifier,
	onClick: () -> Unit,
	onLongClick: (() -> Unit)? = null,
	coverArtId: String?,
	title: String,
	subtitle: String? = null,
	id: String,
	// this parameter is a shitty workaround for shared element
	// transitions being performed when switching between tabs
	// this can just be an empty string if the tab is unknown
	tab: String
) {
	val interactionSource = remember { MutableInteractionSource() }
	with(LocalSharedTransitionScope.current) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.combinedClickable(
					interactionSource = interactionSource,
					indication = null,
					onClick = onClick,
					onLongClick = onLongClick
				)
				.then(modifier)
		) {
			CoverArt(
				coverArtId = coverArtId,
				contentDescription = title,
				modifier = Modifier
					.fillMaxWidth()
					.sharedElement(
						sharedContentState = this@with.rememberSharedContentState("${tab}-${id}-cover"),
						boundsTransform = BoundsTransform { _, _ ->
							tween(
								durationMillis = 500,
								easing = EmphasizedDecelerateEasing
							)
						},
						animatedVisibilityScope = LocalNavAnimatedContentScope.current
					),
				interactionSource = interactionSource
			)
			Text(
				text = title,
				style = MaterialTheme.typography.titleSmallEmphasized,
				modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
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
				// placeholders shouldn't use continuous corners
				// because it's less performant
				.clip(RoundedCornerShape(16.0.dp))
				.shimmerLoading()
		)
		Box(
			modifier = Modifier
				.padding(top = 6.dp)
				.fillMaxWidth(0.8f)
				.height(16.dp)
				.clip(CircleShape)
				.shimmerLoading()
		)
		Box(
			modifier = Modifier
				.padding(top = 4.dp)
				.fillMaxWidth(0.6f)
				.height(14.dp)
				.clip(CircleShape)
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

fun <T> LazyGridScope.artGridError(
	state: UiState.Error<T>
) {
	item(span = { GridItemSpan(maxLineSpan) }) {
		ErrorBox(
			modifier = Modifier.animateItem(fadeInSpec = null),
			error = state
		)
	}
}

package paige.navic.ui.components.layouts

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_see_all
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.shared.systemGesturesExclusion
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun <T> LazyGridScope.horizontalSection(
	seeAll: Boolean,
	title: StringResource,
	destination: NavKey,
	state: UiState<List<T>>,
	key: (T) -> Any,
	itemContent: @Composable LazyItemScope.(T) -> Unit,
) {
	if (state !is UiState.Success || state.data.isNotEmpty()) {
		header(title, destination = destination, active = seeAll)
	}

	when (state) {
		is UiState.Error -> artGridError(state)
		else -> item(span = { GridItemSpan(maxLineSpan) }) {
			LazyRow(
				modifier = Modifier
					.animateContentSize(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec())
					.systemGesturesExclusion(),
				horizontalArrangement = Arrangement.spacedBy(12.dp),
				contentPadding = PaddingValues(horizontal = 16.dp)
			) {
				if (state is UiState.Loading) {
					items(8) {
						ArtGridPlaceholder(Modifier.width(150.dp))
					}
				} else if (state is UiState.Success) {
					items(state.data, key = key) { item ->
						itemContent(item)
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
private fun LazyGridScope.header(
	title: StringResource,
	vararg formatArgs: Any,
	destination: NavKey,
	active: Boolean
) {
	item(span = { GridItemSpan(1) }) {
		Text(
			stringResource(title, formatArgs),
			style = MaterialTheme.typography.titleMediumEmphasized,
			fontWeight = FontWeight(600),
			modifier = Modifier.heightIn(min = 32.dp).padding(top = 8.dp, start = 16.dp)
		)
	}
	if (active) {
		item(span = { GridItemSpan(1) }) {
			val ctx = LocalCtx.current
			val backStack = LocalNavStack.current
			Text(
				stringResource(Res.string.action_see_all),
				fontSize = 12.sp,
				color = MaterialTheme.colorScheme.primary,
				textAlign = TextAlign.Right,
				modifier = Modifier
					.heightIn(min = 32.dp)
					.padding(top = 8.dp, end = 16.dp)
					.clickable(
						interactionSource = null,
						indication = null
					) {
						ctx.clickSound()
						backStack.add(destination)
					}
			)
		}
	}
}
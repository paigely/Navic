package paige.navic.ui.components.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_navigate_back
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedTopBar(
	title: @Composable () -> Unit,
	actions: @Composable RowScope.() -> Unit = {},
	colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
	hideBack: Boolean = false
) {
	val backStack = LocalNavStack.current
	TopAppBar(
		title = title,
		colors = colors,
		actions = {
			Row(
				modifier = Modifier.padding(end = 12.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
				actions()
			}
		},
		navigationIcon = {
			if (!hideBack) {
				TopBarButton(
					modifier = Modifier.padding(horizontal = 12.dp),
					onClick = {
						if (backStack.size > 1) {
							backStack.removeLastOrNull()
						}
					}
				) {
					Icon(
						Icons.Outlined.ArrowBack,
						stringResource(Res.string.action_navigate_back)
					)
				}
			}
		},
	)
}

@Composable
fun TopBarButton(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit
) {
	val ctx = LocalCtx.current
	IconButton(
		modifier = modifier,
		colors = IconButtonDefaults.iconButtonVibrantColors(
			containerColor = MaterialTheme.colorScheme.surfaceContainer
		),
		onClick = {
			ctx.clickSound()
			onClick()
		}
	) {
		content()
	}
}
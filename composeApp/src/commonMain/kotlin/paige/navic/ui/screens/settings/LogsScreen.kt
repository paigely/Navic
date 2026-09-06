package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_scroll_down
import navic.composeapp.generated.resources.title_logs
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.LogManager
import paige.navic.domain.parser.LogLine
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ArrowDown
import paige.navic.icons.outlined.Delete
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton

@Composable
fun SettingsLogsScreen() {
	val logManager = koinInject<LogManager>()
	val lines = remember { mutableStateListOf<LogLine>() }
	val listState = rememberLazyListState()
	val isAtBottom by remember {
		derivedStateOf {
			val layoutInfo = listState.layoutInfo
			val totalItems = layoutInfo.totalItemsCount
			val lastIdx = layoutInfo.visibleItemsInfo.lastOrNull()?.index
			return@derivedStateOf (lastIdx == totalItems - 1) || totalItems == 0
		}
	}
	val scope = rememberCoroutineScope()

	val slideSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
	val scaleInSpec = MaterialTheme.motionScheme.fastSpatialSpec<Float>()

	LaunchedEffect(Unit) {
		logManager.logFlow().cancellable().collect {
			lines += it
			if (isAtBottom) {
				listState.requestScrollToItem(lines.lastIndex)
			}
			if (lines.size > 500) lines.removeAt(0)
		}
	}

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(Res.string.title_logs)) },
				actions = {
					TopBarButton(
						onClick = {
							lines.clear()
							logManager.clearLogs()
						},
						enabled = lines.isNotEmpty()
					) {
						Icon(Icons.Outlined.Delete, stringResource(Res.string.action_delete))
					}
				}
			)
		},
		floatingActionButton = {
			AnimatedContent(
				targetState = isAtBottom,
				transitionSpec = {
					val transformOrigin = TransformOrigin(0f, 1f)
					(slideInHorizontally(slideSpec) { it / 2 }
						+ scaleIn(scaleInSpec, transformOrigin = transformOrigin)
						+ slideInVertically(slideSpec) { it / 2 })
						.togetherWith(slideOutHorizontally(slideSpec) { it / 2 }
							+ scaleOut(transformOrigin = transformOrigin)
							+ slideOutVertically(slideSpec) { it / 2 })
						.using(SizeTransform(clip = false))
				}
			) { isAtBottom ->
				if (!isAtBottom) {
					FloatingActionButton(
						shape = MaterialTheme.shapes.large,
						containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
						onClick = dropUnlessResumed {
							scope.launch {
								listState.animateScrollToItem(lines.lastIndex)
							}
						}
					) {
						Icon(
							imageVector = Icons.Outlined.ArrowDown,
							contentDescription = stringResource(Res.string.action_scroll_down),
							modifier = Modifier.size(26.dp)
						)
					}
				}
			}
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			LazyColumn(
				modifier = Modifier.horizontalScroll(rememberScrollState()),
				state = listState,
				contentPadding = innerPadding
			) {
				items(
					items = lines.toImmutableList(),
					key = { it.id }
				) { line ->
					LogLineRow(line = line)
				}
			}
		}
	}
}

@Composable
private fun LogLineRow(
	line: LogLine
) {
	@Suppress("DEPRECATION")
	val clipboardManager = LocalClipboardManager.current
	Surface(
		onClick = {
			clipboardManager.setText(AnnotatedString(line.rawText))
		}
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically
		) {
			Box(
				modifier = Modifier
					.padding(horizontal = 3.dp, vertical = 1.5.dp)
					.size(22.dp)
					.clip(MaterialTheme.shapes.extraSmall)
					.background(line.type.backgroundColor()),
				contentAlignment = Alignment.Center
			) {
				Text(
					text = line.type.name.first().toString(),
					fontSize = 12.sp,
					color = line.type.contentColor()
				)
			}

			Text(
				text = line.text,
				fontFamily = FontFamily.Monospace,
				fontSize = 12.sp,
				maxLines = 1
			)
		}
	}
}

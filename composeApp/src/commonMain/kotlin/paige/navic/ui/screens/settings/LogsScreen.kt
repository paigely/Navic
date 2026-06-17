package paige.navic.ui.screens.settings

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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.title_logs
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.LogManager
import paige.navic.domain.parser.LogLine
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Delete
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton

@Composable
fun SettingsLogsScreen() {
	val logManager = koinInject<LogManager>()
	val logs = logManager.logs
	val listState = rememberLazyListState()

	DisposableEffect(Unit) {
		logManager.startStreaming()
		onDispose {
			logManager.stopStreaming()
		}
	}

	LaunchedEffect(logs.size) {
		if (logs.lastIndex > 0) {
			listState.requestScrollToItem(logs.lastIndex)
		}
	}

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(Res.string.title_logs)) },
				actions = {
					TopBarButton(
						onClick = { logManager.clearLogs() },
						enabled = logs.isNotEmpty()
					) {
						Icon(Icons.Outlined.Delete, stringResource(Res.string.action_delete))
					}
				}
			)
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
				items(logs) { line ->
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

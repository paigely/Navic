package paige.navic.ui.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Check
import paige.navic.icons.outlined.Copy

@Composable
fun ErrorCodeBlock(error: Throwable) {

	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current
	val ctx = LocalCtx.current
	var copied by remember { mutableStateOf(false) }

	LaunchedEffect(copied) {
		if (copied) {
			delay(2000)
			copied = false
		}
	}

	Box {
		SelectionContainer(
			Modifier
				.background(
					MaterialTheme.colorScheme.surfaceContainer,
					MaterialTheme.shapes.extraSmall
				)
				.padding(8.dp)
				.horizontalScroll(rememberScrollState())
		) {
			Text(
				error.stackTraceToString(),
				fontFamily = FontFamily.Monospace,
				fontSize = 10.sp,
				lineHeight = 10.sp,
				color = MaterialTheme.colorScheme.onSurface,
				modifier = Modifier.padding(end = 48.dp)
			)
		}
		IconButton(
			modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
			onClick = {
				ctx.clickSound()
				clipboard.setText(AnnotatedString(error.stackTraceToString()))
				copied = true
			},
			content = {
				Icon(
					if (copied) Icons.Outlined.Check else Icons.Outlined.Copy,
					null
				)
			},
			colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surfaceContainerHighest)
		)
	}
}
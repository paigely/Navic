package paige.navic.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousRoundedRectangle
import com.materialkolor.ktx.darken
import kotlinx.coroutines.delay
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.check
import navic.composeapp.generated.resources.copy
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.keyboard_arrow_down
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalCtx
import paige.navic.ui.theme.mapleMono
import paige.navic.util.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorBox(
	error: UiState.Error,
	padding: PaddingValues = PaddingValues(12.dp),
	modifier: Modifier = Modifier
) {
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current
	val ctx = LocalCtx.current
	var expanded by remember { mutableStateOf(false) }
	val iconScale by animateFloatAsState(
		if (expanded)
			-1f
		else 1f,
		animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
	)
	var copied by remember { mutableStateOf(false) }
	LaunchedEffect(copied) {
		if (copied) {
			delay(2000)
			copied = false
		}
	}
	Form(modifier = modifier.padding(padding)) {
		FormRow(
			color = MaterialTheme.colorScheme.errorContainer
		) {
			Text(
				stringResource(Res.string.info_error)
			)
			IconButton(
				onClick = {
					ctx.clickSound()
					expanded = !expanded
				},
				content = {
					Icon(
						vectorResource(Res.drawable.keyboard_arrow_down),
						null,
						modifier = Modifier.scale(scaleX = 1f, scaleY = iconScale)
					)
				},
				colors = IconButtonDefaults.iconButtonColors(
					MaterialTheme.colorScheme.errorContainer.darken(1.25f)
				)
			)
		}
		AnimatedVisibility(expanded) {
			Box {
				SelectionContainer(
					Modifier
						.background(
							MaterialTheme.colorScheme.surfaceContainer,
							ContinuousRoundedRectangle(3.dp)
						)
						.padding(8.dp)
						.horizontalScroll(rememberScrollState())
				) {
					Text(
						error.error.stackTraceToString(),
						fontFamily = mapleMono(),
						fontSize = 12.sp,
						color = MaterialTheme.colorScheme.onSurface
					)
				}
				IconButton(
					modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
					onClick = {
						ctx.clickSound()
						clipboard.setText(AnnotatedString(error.error.stackTraceToString()))
						copied = true
					},
					content = {
						Icon(
							vectorResource(if (copied) Res.drawable.check else Res.drawable.copy),
							null
						)
					},
					colors = IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.surfaceContainerHighest)
				)
			}
		}
	}
}

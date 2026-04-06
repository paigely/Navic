package paige.navic.ui.components.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.materialkolor.ktx.darken
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_error
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.outlined.KeyboardArrowDown
import paige.navic.icons.outlined.Refresh
import paige.navic.shared.Logger
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T>ErrorBox(
	error: UiState.Error<T>,
	padding: PaddingValues = PaddingValues(12.dp),
	onRetry: (() -> Unit)? = null,
	modifier: Modifier = Modifier
) {
	val ctx = LocalCtx.current
	var expanded by remember { mutableStateOf(false) }
	val iconScale by animateFloatAsState(
		if (expanded)
			-1f
		else 1f,
		animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()
	)

	LaunchedEffect(error.error) {
		Logger.e("ErrorBox", "Printing stack trace for error", error.error)
	}

	Form(modifier = modifier.padding(padding)) {
		FormRow(
			color = MaterialTheme.colorScheme.errorContainer,
			horizontalArrangement = Arrangement.Center
		) {
			Text(
				stringResource(Res.string.info_error),
				modifier = Modifier.weight(1f)
			)
			onRetry?.let { onRetry ->
				IconButton(
					onClick = {
						ctx.clickSound()
						onRetry()
					},
					content = {
						Icon(Icons.Outlined.Refresh, null)
					},
					colors = IconButtonDefaults.iconButtonColors(
						MaterialTheme.colorScheme.errorContainer.darken(1.25f)
					)
				)
			}
			IconButton(
				onClick = {
					ctx.clickSound()
					expanded = !expanded
				},
				content = {
					Icon(
						Icons.Outlined.KeyboardArrowDown,
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
			ErrorCodeBlock(error.error)
		}
	}
}

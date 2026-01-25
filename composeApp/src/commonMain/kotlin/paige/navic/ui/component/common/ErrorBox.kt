package paige.navic.ui.component.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousRoundedRectangle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.info_error_hide
import navic.composeapp.generated.resources.info_error_show
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.ui.theme.mapleMono
import paige.navic.util.UiState

@Composable
fun ErrorBox(
	error: UiState.Error,
	modifier: Modifier = Modifier
) {
	val ctx = LocalCtx.current
	var expanded by remember { mutableStateOf(false) }
	Form(modifier = modifier.padding(12.dp)) {
		FormRow(
			color = MaterialTheme.colorScheme.errorContainer
		) {
			Text(
				stringResource(Res.string.info_error)
			)
			TextButton(
				onClick = {
					ctx.clickSound()
					expanded = !expanded
				},
				content = {
					Text(
						stringResource(
							if (!expanded)
								Res.string.info_error_show
							else Res.string.info_error_hide
						)
					)
				}
			)
		}
		AnimatedVisibility(expanded) {
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
		}
	}
}

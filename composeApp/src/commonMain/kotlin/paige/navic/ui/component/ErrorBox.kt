package paige.navic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.info_error_hide
import navic.composeapp.generated.resources.info_error_show
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.util.UiState

@Composable
fun ErrorBox(error: UiState.Error) {
	val ctx = LocalCtx.current
	val scrollState = rememberScrollState()
	var expanded by remember { mutableStateOf(false) }
	Column(
		modifier = Modifier
			.fillMaxSize()
			.verticalScroll(scrollState)
	) {
		Form(modifier = Modifier.padding(12.dp)) {
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
						Text(stringResource(
							if (!expanded)
								Res.string.info_error_show
							else Res.string.info_error_hide
						))
					}
				)
			}
			if (expanded) {
				FormRow(
					color = MaterialTheme.colorScheme.errorContainer
				) {
					SelectionContainer {
						Text("$error")
					}
				}
			}
		}
	}
}

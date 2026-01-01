package paige.navic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import paige.navic.LocalCtx
import paige.navic.data.session.SessionManager
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
					"Something went wrong.\n"
						+ (if (SessionManager.currentUser != null)
						"Make sure you can connect to\nthe internet"
					else "You probably need to log in")
				)
				TextButton(
					onClick = {
						ctx.clickSound()
						expanded = !expanded
					},
					content = { Text(if (!expanded) "Details" else "Hide details") }
				)
			}
			if (expanded) {
				FormRow(
					color = MaterialTheme.colorScheme.errorContainer
				) {
					Text("$error")
				}
			}
		}
	}
}

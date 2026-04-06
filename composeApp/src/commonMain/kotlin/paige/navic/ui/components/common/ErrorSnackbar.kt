package paige.navic.ui.components.common

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_ok
import navic.composeapp.generated.resources.info_error
import navic.composeapp.generated.resources.info_error_show
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalSnackbarState
import paige.navic.shared.Logger
import paige.navic.ui.components.dialogs.FormDialog

@Composable
fun ErrorSnackbar(
	error: Throwable?,
	onClearError: () -> Unit
) {
	if (error == null) return

	val snackbarState = LocalSnackbarState.current
	var visible by rememberSaveable { mutableStateOf(false) }

	LaunchedEffect(error) {
		val result = snackbarState.showSnackbar(
			message = getString(Res.string.info_error),
			actionLabel = getString(Res.string.info_error_show),
			duration = SnackbarDuration.Long
		)
		if (result == SnackbarResult.ActionPerformed) {
			visible = true
			Logger.e("ErrorSnackbar", "Printing stack trace for error", error)
		} else {
			onClearError()
		}
	}

	if (!visible) return

	FormDialog(
		onDismissRequest = {
			visible = false
			onClearError()
		},
		buttons = {
			FormButton(onClick = {
				visible = false
				onClearError()
			}) {
				Text(stringResource(Res.string.action_ok))
			}
		}
	) {
		ErrorCodeBlock(error)
	}
}
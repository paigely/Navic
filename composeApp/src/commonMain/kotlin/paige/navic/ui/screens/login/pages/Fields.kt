package paige.navic.ui.screens.login.pages

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_account_navidrome_instance
import navic.composeapp.generated.resources.option_account_password
import navic.composeapp.generated.resources.option_account_username
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreenFields(
	isBusy: Boolean,
	instanceState: TextFieldState,
	instanceError: Boolean,
	instanceFocusRequester: FocusRequester,
	onInstanceFocusChanged: () -> Unit,
	usernameState: TextFieldState,
	usernameError: Boolean,
	usernameFocusRequester: FocusRequester,
	onUsernameFocusChanged: () -> Unit,
	passwordState: TextFieldState,
	passwordError: Boolean,
	passwordFocusRequester: FocusRequester,
	onPasswordFocusChanged: () -> Unit
) {
	var instanceWasFocused by remember { mutableStateOf(false) }
	OutlinedTextField(
		modifier = Modifier
			.height(60.dp)
			.padding(horizontal = 16.dp)
			.fillMaxWidth()
			.focusRequester(instanceFocusRequester)
			.onFocusChanged { state ->
				if (state.isFocused) {
					instanceWasFocused = true
				}
				if (instanceWasFocused && !state.isFocused) onInstanceFocusChanged()
			},
		state = instanceState,
		isError = instanceError,
		label = { Text(stringResource(Res.string.option_account_navidrome_instance)) },
		lineLimits = TextFieldLineLimits.SingleLine,
		enabled = !isBusy,
		keyboardOptions = KeyboardOptions(
			autoCorrectEnabled = false,
			keyboardType = KeyboardType.Uri
		)
	)

	LoginScreenSuggestionChips(instanceState = instanceState)

	var usernameWasFocused by remember { mutableStateOf(false) }
	OutlinedTextField(
		modifier = Modifier
			.height(60.dp)
			.padding(horizontal = 16.dp)
			.fillMaxWidth()
			.focusRequester(usernameFocusRequester)
			.onFocusChanged { state ->
				if (state.isFocused) {
					usernameWasFocused = true
				}
				if (usernameWasFocused && !state.isFocused) onUsernameFocusChanged()
			}
			.semantics {
				contentType = ContentType.Username
			},
		state = usernameState,
		isError = usernameError,
		label = { Text(stringResource(Res.string.option_account_username)) },
		lineLimits = TextFieldLineLimits.SingleLine,
		enabled = !isBusy,
		keyboardOptions = KeyboardOptions(
			autoCorrectEnabled = false
		)
	)

	var passwordWasFocused by remember { mutableStateOf(false) }
	OutlinedSecureTextField(
		modifier = Modifier
			.height(60.dp)
			.padding(horizontal = 16.dp)
			.fillMaxWidth()
			.focusRequester(passwordFocusRequester)
			.onFocusChanged { state ->
				if (state.isFocused) {
					passwordWasFocused = true
				}
				if (passwordWasFocused && !state.isFocused) onPasswordFocusChanged()
			}
			.semantics {
				contentType = ContentType.Password
			},
		state = passwordState,
		isError = passwordError,
		label = { Text(stringResource(Res.string.option_account_password)) },
		enabled = !isBusy,
		keyboardOptions = KeyboardOptions(
			autoCorrectEnabled = false,
			keyboardType = KeyboardType.Password,
		)
	)
}

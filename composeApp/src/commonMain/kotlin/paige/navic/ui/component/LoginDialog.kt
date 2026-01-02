package paige.navic.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.burnoo.compose.remembersetting.rememberStringSetting
import paige.navic.LocalCtx
import paige.navic.data.model.User
import paige.navic.ui.viewmodel.TopBarViewModel
import paige.navic.util.LoginState

@Composable
fun LoginDialog(
	userState: LoginState<User?>,
	viewModel: TopBarViewModel,
	visible: Boolean,
	setVisible: (Boolean) -> Unit
) {
	val ctx = LocalCtx.current
	var instanceUrl by rememberStringSetting("instanceUrl", "")
	var username by rememberStringSetting("username", "")
	var password by rememberStringSetting("password", "")
	val scrollState = rememberScrollState()
	if (visible) {
		AlertDialog(
			onDismissRequest = { setVisible(userState !is LoginState.Loading) },
			text = {
				Column(modifier = Modifier.verticalScroll(scrollState)) {
					(userState as? LoginState.Error)?.let {
						Text(it.error.message ?: "$it")
					}
					OutlinedTextField(
						value = instanceUrl,
						onValueChange = { instanceUrl = it },
						label = { Text("Instance") },
						placeholder = { Text("demo.navidrome.org") },
						maxLines = 1,
						keyboardOptions = KeyboardOptions(
							autoCorrectEnabled = false,
							keyboardType = KeyboardType.Uri
						)
					)
					OutlinedTextField(
						value = username,
						onValueChange = { username = it },
						label = { Text("Username") },
						maxLines = 1,
						modifier = Modifier.semantics {
							contentType = ContentType.Username
						},
						keyboardOptions = KeyboardOptions(
							autoCorrectEnabled = false
						)
					)
					OutlinedTextField(
						value = password,
						onValueChange = { password = it },
						label = { Text("Password") },
						visualTransformation = PasswordVisualTransformation(),
						maxLines = 1,
						modifier = Modifier.semantics {
							contentType = ContentType.Password
						},
						keyboardOptions = KeyboardOptions(
							autoCorrectEnabled = false,
							keyboardType = KeyboardType.Password
						)
					)
				}
			},
			confirmButton = {
				Button(
					shape = ContinuousCapsule,
					onClick = {
						viewModel.login(
							if (
								!instanceUrl.startsWith("https://")
								&& !instanceUrl.startsWith("http://")
							) "https://$instanceUrl" else instanceUrl,
							username,
							password
						)
					},
					enabled = userState !is LoginState.Loading,
					content = {
						if (userState !is LoginState.Loading) {
							Text("Login")
						} else {
							CircularProgressIndicator(
								modifier = Modifier.size(20.dp)
							)
						}
					}
				)
			},
			dismissButton = {
				TextButton(
					onClick = {
						ctx.clickSound()
						setVisible(false)
					},
					enabled = userState !is LoginState.Loading,
					content = { Text("Cancel") }
				)
			},
			shape = ContinuousRoundedRectangle(42.dp)
		)
	}
}

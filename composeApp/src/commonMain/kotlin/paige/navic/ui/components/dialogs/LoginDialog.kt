package paige.navic.ui.components.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.option_account_navidrome_instance
import navic.composeapp.generated.resources.option_account_password
import navic.composeapp.generated.resources.option_account_username
import navic.composeapp.generated.resources.title_login_dialog
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Badge
import paige.navic.icons.outlined.Link
import paige.navic.icons.outlined.Login
import paige.navic.icons.outlined.Password
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.theme.defaultFont
import paige.navic.ui.viewmodels.LoginViewModel
import paige.navic.utils.LoginState
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginDialog(
	viewModel: LoginViewModel = viewModel { LoginViewModel() },
	onDismissRequest: () -> Unit
) {
	val ctx = LocalCtx.current
	val scrollState = rememberScrollState()

	val loginState by viewModel.loginState.collectAsState()
	val linkColor = MaterialTheme.colorScheme.primary
	val noticeText = remember {
		buildAnnotatedString {
			append("Navic needs an instance of Navidrome to function. ")
			append("Learn about Navidrome ")
			withLink(LinkAnnotation.Url(url = "https://www.navidrome.org/")) {
				withStyle(SpanStyle(color = linkColor)) {
					append("here!")
				}
			}
		}
	}

	val spatialSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
	val effectSpec = MaterialTheme.motionScheme.slowEffectsSpec<Float>()

	AlertDialog(
		onDismissRequest = {
			if (loginState !is LoginState.Loading) {
				onDismissRequest()
			}
		},
		title = { Text(stringResource(Res.string.title_login_dialog)) },
		text = {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.verticalScroll(scrollState)
			) {
				AnimatedContent(
					(loginState as? LoginState.Error),
					modifier = Modifier.fillMaxWidth(),
					transitionSpec = {
						(fadeIn(
							animationSpec = effectSpec
						) + scaleIn(
							initialScale = 0.8f,
							animationSpec = spatialSpec
						)) togetherWith (fadeOut(
							animationSpec = effectSpec
						) + scaleOut(
							animationSpec = spatialSpec
						))
					}
				) {
					if (it != null) {
						ErrorBox(
							UiState.Error(it.error),
							padding = PaddingValues(0.dp),
							modifier = Modifier.fillMaxWidth()
						)
					}
				}
				Spacer(Modifier.height(2.dp))
				Text(noticeText)
				Spacer(Modifier.height(2.dp))
				OutlinedTextField(
					state = viewModel.instanceState,
					leadingIcon = { Icon(Icons.Outlined.Link, null) },
					label = { Text(stringResource(Res.string.option_account_navidrome_instance)) },
					placeholder = { Text("demo.navidrome.org") },
					lineLimits = TextFieldLineLimits.SingleLine,
					modifier = Modifier.fillMaxWidth(),
					keyboardOptions = KeyboardOptions(
						autoCorrectEnabled = false,
						keyboardType = KeyboardType.Uri
					)
				)
				Spacer(Modifier.height(8.dp))
				OutlinedTextField(
					state = viewModel.usernameState,
					leadingIcon = { Icon(Icons.Outlined.Badge, null) },
					label = { Text(stringResource(Res.string.option_account_username)) },
					lineLimits = TextFieldLineLimits.SingleLine,
					modifier = Modifier.fillMaxWidth().semantics {
						contentType = ContentType.Username
					},
					keyboardOptions = KeyboardOptions(
						autoCorrectEnabled = false
					)
				)
				OutlinedSecureTextField(
					state = viewModel.passwordState,
					leadingIcon = { Icon(Icons.Outlined.Password, null) },
					label = { Text(stringResource(Res.string.option_account_password)) },
					modifier = Modifier.fillMaxWidth()
				)
			}
		},
		confirmButton = {
			Button(
				shape = ContinuousCapsule,
				onClick = {
					viewModel.login()
				},
				enabled = loginState !is LoginState.Loading,
				content = {
					if (loginState !is LoginState.Loading) {
						Icon(Icons.Outlined.Login, null)
						Spacer(Modifier.width(8.dp))
						Text(stringResource(Res.string.action_log_in), fontFamily = defaultFont(100))
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
					onDismissRequest()
				},
				enabled = loginState !is LoginState.Loading,
				content = { Text(stringResource(Res.string.action_cancel)) }
			)
		},
		shape = MaterialTheme.shapes.extraExtraLarge
	)
}
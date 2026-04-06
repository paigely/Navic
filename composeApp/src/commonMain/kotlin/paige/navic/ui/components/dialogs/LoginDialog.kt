package paige.navic.ui.components.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.notice_login_suggestion
import navic.composeapp.generated.resources.option_account_navidrome_instance
import navic.composeapp.generated.resources.option_account_password
import navic.composeapp.generated.resources.option_account_username
import navic.composeapp.generated.resources.title_login_dialog
import org.jetbrains.compose.resources.stringResource
import paige.navic.data.models.User
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Badge
import paige.navic.icons.outlined.Link
import paige.navic.icons.outlined.Password
import paige.navic.ui.components.common.ErrorBox
import paige.navic.ui.components.common.FormButton
import paige.navic.utils.LoginState
import paige.navic.utils.UiState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun LoginDialog(
	loginState: LoginState<User?>,
	instanceState: TextFieldState,
	usernameState: TextFieldState,
	passwordState: TextFieldState,
	onLogin: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val isBusy = loginState is LoginState.Loading || loginState is LoginState.Syncing

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

	FormDialog(
		title = { Text(stringResource(Res.string.title_login_dialog)) },
		buttons = {
			FormButton(
				onClick = onLogin,
				color = MaterialTheme.colorScheme.primary,
				enabled = !isBusy
			) {
				if (loginState is LoginState.Loading) {
					CircularProgressIndicator(Modifier.size(20.dp))
				}
				Text(stringResource(Res.string.action_log_in))
			}
			FormButton(
				onClick = onDismissRequest,
				enabled = !isBusy
			) {
				Text(stringResource(Res.string.action_cancel))
			}
		},
		onDismissRequest = {
			if (!isBusy) {
				onDismissRequest()
			}
		}
	) {
		Column(
			modifier = Modifier.fillMaxWidth()
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
						UiState.Error(it.error, null),
						padding = PaddingValues(0.dp),
						modifier = Modifier.fillMaxWidth()
					)
				}
			}

			AnimatedVisibility(
				visible = loginState is LoginState.Syncing,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				val syncState = loginState as? LoginState.Syncing
				Column(modifier = Modifier.fillMaxWidth()) {
					Spacer(Modifier.height(8.dp))
					Text(
						text = syncState?.message ?: "Syncing...",
						style = MaterialTheme.typography.bodySmall,
						color = MaterialTheme.colorScheme.primary
					)
					Spacer(Modifier.height(4.dp))
					LinearProgressIndicator(
						progress = { syncState?.progress ?: 0f },
						modifier = Modifier.fillMaxWidth()
					)
					Spacer(Modifier.height(8.dp))
				}
			}

			Spacer(Modifier.height(2.dp))
			Text(noticeText)
			Spacer(Modifier.height(2.dp))

			OutlinedTextField(
				state = instanceState,
				leadingIcon = { Icon(Icons.Outlined.Link, null) },
				label = { Text(stringResource(Res.string.option_account_navidrome_instance)) },
				placeholder = { Text("demo.navidrome.org") },
				lineLimits = TextFieldLineLimits.SingleLine,
				modifier = Modifier.fillMaxWidth(),
				enabled = !isBusy,
				keyboardOptions = KeyboardOptions(
					autoCorrectEnabled = false,
					keyboardType = KeyboardType.Uri
				)
			)

			val showSuggestions = instanceState.text.isNotEmpty() &&
					!instanceState.text.startsWith("http://") &&
					!instanceState.text.startsWith("https://") &&
					instanceState.text.contains(".")

			AnimatedVisibility(
				visible = showSuggestions,
				enter = expandVertically() + fadeIn(),
				exit = shrinkVertically() + fadeOut()
			) {
				Column {
					Spacer(Modifier.height(4.dp))
					Text(
						text = stringResource(Res.string.notice_login_suggestion),
						style = MaterialTheme.typography.labelMedium,
						color = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Spacer(Modifier.height(4.dp))
					Row(
						modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
						horizontalArrangement = Arrangement.spacedBy(8.dp)
					) {
						val url = instanceState.text.toString()
						SuggestionChip(
							onClick = {
								instanceState.edit {
									replace(0, length, "https://$url")
								}
							},
							label = {
								Text(
									text = "https://$url",
									style = MaterialTheme.typography.labelSmall,
									maxLines = 1
								)
							}
						)
						SuggestionChip(
							onClick = {
								instanceState.edit {
									replace(0, length, "http://$url")
								}
							},
							label = {
								Text(
									text = "http://$url",
									style = MaterialTheme.typography.labelSmall,
									maxLines = 1
								)
							}
						)
					}
				}
			}

			Spacer(Modifier.height(8.dp))
			OutlinedTextField(
				state = usernameState,
				leadingIcon = { Icon(Icons.Outlined.Badge, null) },
				label = { Text(stringResource(Res.string.option_account_username)) },
				lineLimits = TextFieldLineLimits.SingleLine,
				enabled = !isBusy,
				modifier = Modifier.fillMaxWidth().semantics {
					contentType = ContentType.Username
				},
				keyboardOptions = KeyboardOptions(
					autoCorrectEnabled = false
				)
			)
			OutlinedSecureTextField(
				state = passwordState,
				leadingIcon = { Icon(Icons.Outlined.Password, null) },
				label = { Text(stringResource(Res.string.option_account_password)) },
				enabled = !isBusy,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}
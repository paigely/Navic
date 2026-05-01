package paige.navic.ui.screens.login.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.dropUnlessResumed
import com.kyant.capsule.ContinuousCapsule
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_log_in
import navic.composeapp.generated.resources.info_login_description_end
import navic.composeapp.generated.resources.info_login_description_middle
import navic.composeapp.generated.resources.info_login_description_start
import navic.composeapp.generated.resources.option_custom_headers
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.ui.screens.login.viewmodels.LoginViewModel
import paige.navic.ui.theme.defaultFont
import paige.navic.utils.LoginState

@Composable
fun LoginScreenContent(innerPadding: PaddingValues) {
	val viewModel = koinViewModel<LoginViewModel>()
	val loginState by viewModel.loginState.collectAsStateWithLifecycle()

	val instanceState = viewModel.instanceState
	val usernameState = viewModel.usernameState
	val passwordState = viewModel.passwordState

	val isBusy = loginState is LoginState.Loading || loginState is LoginState.Syncing

	val linkColor = MaterialTheme.colorScheme.primary
	val startText = stringResource(Res.string.info_login_description_start)
	val middleText = stringResource(Res.string.info_login_description_middle)
	val endText = stringResource(Res.string.info_login_description_end)
	val noticeText = remember {
		buildAnnotatedString {
			append("$startText ")
			withLink(LinkAnnotation.Url(url = "https://www.navidrome.org/")) {
				withStyle(SpanStyle(color = linkColor)) {
					append(middleText)
				}
			}
			append(" $endText")
		}
	}

	val ctx = LocalCtx.current
	val haptics = LocalHapticFeedback.current
	val backStack = LocalNavStack.current
	val focusManager = LocalFocusManager.current

	val instanceFocusRequester = remember { FocusRequester() }
	val usernameFocusRequester = remember { FocusRequester() }
	val passwordFocusRequester = remember { FocusRequester() }

	val login = {
		if (!viewModel.login()) {
			haptics.performHapticFeedback(HapticFeedbackType.Reject)
			when {
				viewModel.instanceError -> instanceFocusRequester.requestFocus()
				viewModel.usernameError -> usernameFocusRequester.requestFocus()
				viewModel.passwordError -> passwordFocusRequester.requestFocus()
			}
		}
	}

	LaunchedEffect(loginState) {
		if (loginState is LoginState.Success) {
			backStack.clear()
			backStack.add(Screen.Library())
		}
	}

	Box {
		LoginScreenProgress(
			modifier = Modifier
				.align(Alignment.TopCenter)
				.padding(top = innerPadding.calculateTopPadding()),
			isBusy = isBusy,
			loginState = loginState
		)

		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(innerPadding)
				.consumeWindowInsets(innerPadding)
				.imePadding(),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(6.dp)
		) {
			Column(
				modifier = Modifier
					.weight(1f)
					.widthIn(max = 600.dp)
					.verticalScroll(rememberScrollState())
			) {
				Spacer(Modifier.weight(1f))

				Text(
					text = stringResource(Res.string.action_log_in),
					style = MaterialTheme.typography.headlineMedium,
					fontFamily = defaultFont(round = 100f),
					modifier = Modifier.padding(horizontal = 16.dp)
				)
				Text(
					text = noticeText,
					modifier = Modifier.padding(horizontal = 16.dp)
				)

				Spacer(Modifier.height(8.dp))

				LoginScreenError(loginState = loginState)

				LoginScreenFields(
					isBusy = isBusy,
					instanceState = instanceState,
					instanceError = viewModel.instanceError,
					instanceFocusRequester = instanceFocusRequester,
					onInstanceFocusChanged = { viewModel.validateInstance() },
					usernameState = usernameState,
					usernameError = viewModel.usernameError,
					usernameFocusRequester = usernameFocusRequester,
					onUsernameFocusChanged = { viewModel.validateUsername() },
					passwordState = passwordState,
					passwordError = viewModel.passwordError,
					passwordFocusRequester = passwordFocusRequester,
					onPasswordFocusChanged = { viewModel.validatePassword() },
					onLogin = login
				)

				Spacer(Modifier.height(12.dp))

				Text(
					text = stringResource(Res.string.option_custom_headers),
					color = MaterialTheme.colorScheme.primary,
					textDecoration = TextDecoration.Underline,
					modifier = Modifier
						.padding(horizontal = 16.dp)
						.clickable(onClick = dropUnlessResumed {
							backStack.lastOrNull()?.let {
								if (it is Screen.Login) {
									ctx.clickSound()
									backStack.add(Screen.Settings.CustomHeaders)
									focusManager.clearFocus(true)
								}
							}
						})
				)

				Spacer(Modifier.weight(2.25f))
			}

			Column(
				modifier = Modifier
					.widthIn(max = 600.dp)
					.padding(horizontal = 16.dp)
					.padding(bottom = 8.dp)
			) {
				LoginScreenSyncStatus(loginState = loginState)
				Button(
					modifier = Modifier.fillMaxWidth(),
					onClick = {
						ctx.clickSound()
						login()
					},
					enabled = !isBusy,
					shape = ContinuousCapsule
				) {
					Text(
						text = stringResource(Res.string.action_log_in),
						fontFamily = defaultFont(100)
					)
				}
			}
		}
	}
}

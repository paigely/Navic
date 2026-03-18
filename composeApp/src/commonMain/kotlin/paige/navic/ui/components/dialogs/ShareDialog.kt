package paige.navic.ui.components.dialogs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_cancel
import navic.composeapp.generated.resources.action_share
import navic.composeapp.generated.resources.notice_copied
import navic.composeapp.generated.resources.notice_expiry
import navic.composeapp.generated.resources.option_share_expires
import navic.composeapp.generated.resources.title_create_share
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalSnackbarState
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.DurationPicker
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormButton
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.settings.SettingSwitchRow
import paige.navic.utils.UiState
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class ShareViewModel : ViewModel() {
	private val _state = MutableStateFlow<UiState<String?>>(UiState.Success(null))
	val state = _state.asStateFlow()

	fun share(
		id: String,
		expiry: Duration?
	) {
		viewModelScope.launch {
			_state.value = UiState.Loading
			try {
				val expiration = expiry?.let { Clock.System.now() + it  }
				val url = SessionManager.api
					.createShare(listOf(id), expiresAt = expiration)
					.url
				_state.value = UiState.Success(url)
			} catch(e: Exception) {
				_state.value = UiState.Error(e)
			}
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ShareDialog(
	viewModel: ShareViewModel = viewModel { ShareViewModel() },
	id: String?,
	onIdClear: () -> Unit,
	expiry: Duration?,
	onExpiryChange: (expiry: Duration?) -> Unit
) {

	// There is not an elegant cross platform way of making a ClipEntry yet this is deprecated lmao
	@Suppress("DEPRECATION")
	val clipboard = LocalClipboardManager.current

	val snackbarState = LocalSnackbarState.current
	val scrollState = rememberScrollState()
	val state by viewModel.state.collectAsState()

	LaunchedEffect(state) {
		if (state is UiState.Success && id != null) {
			viewModel.viewModelScope.launch {
				val link = (state as? UiState.Success<String?>)?.data
					?: return@launch
				onIdClear()
				clipboard.setText(AnnotatedString(link))
				snackbarState.showSnackbar(
					message = buildString {
						append(getString(Res.string.notice_copied))
						expiry?.let {
							append(
								"\n" + getString(
									Res.string.notice_expiry, expiry.toString()
								)
							)
						}
					}
				)
			}
		}
	}

	id?.let {
		FormDialog(
			icon = { Icon(Icons.Outlined.Share, null) },
			title = { Text(stringResource(Res.string.title_create_share)) },
			buttons = {
				FormButton(
					onClick = { viewModel.share(id, expiry) },
					color = MaterialTheme.colorScheme.primary
				) {
					if (state is UiState.Loading) {
						CircularProgressIndicator(Modifier.size(20.dp))
					}
					Text(stringResource(Res.string.action_share))
				}
				FormButton(onClick = onIdClear) {
					Text(stringResource(Res.string.action_cancel))
				}
			},
			onDismissRequest = {
				if (state !is UiState.Loading) {
					onIdClear()
				}
			}
		) {
			Spacer(Modifier.height(12.dp))
			(state as? UiState.Error)?.error?.let {
				SelectionContainer {
					Text("$it")
				}
			}
			Form(bottomPadding = 0.dp) {
				SettingSwitchRow(
					title = { Text(stringResource(Res.string.option_share_expires)) },
					enabled = state !is UiState.Loading,
					value = expiry != null,
					onSetValue = {
						onExpiryChange(if (it) 1.hours else null)
					},
					contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
				)
				expiry?.let {
					FormRow {
						DurationPicker(
							duration = expiry,
							onDurationChange = onExpiryChange,
							enabled = state !is UiState.Loading,
						)
					}
				}
			}
		}
	}
}
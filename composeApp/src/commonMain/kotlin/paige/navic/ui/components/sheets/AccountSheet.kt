package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousCapsule
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_log_out
import navic.composeapp.generated.resources.action_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer_enabled
import navic.composeapp.generated.resources.action_view_shares
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalNavStack
import paige.navic.domain.manager.SleepTimerManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Bedtime
import paige.navic.icons.outlined.Logout
import paige.navic.icons.outlined.Share
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.Monogram
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.login.viewmodels.LoginViewModel
import paige.navic.ui.theme.positive
import paige.navic.util.core.label

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheet(
	onDismissRequest: () -> Unit
) {
	val backStack = LocalNavStack.current
	val loginViewModel = koinViewModel<LoginViewModel>()
	val settings = koinInject<Settings>()

	var sleepTimerSheetOpen by rememberSaveable { mutableStateOf(false) }
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val sleepTimerLeft = sleepTimerManager.timeLeft

	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

	val scope = rememberCoroutineScope()
	val animateToDismiss = {
		scope
			.launch { sheetState.hide() }
			.invokeOnCompletion {
				if (!sheetState.isVisible) {
					onDismissRequest()
				}
			}
	}

	ModalBottomSheet(
		onDismissRequest = onDismissRequest,
		sheetState = sheetState,
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					left = 12.dp,
					right = 12.dp
				)
			)
		},
		dragHandle = {
			Surface(
				modifier = Modifier.padding(vertical = 6.dp),
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				shape = ContinuousCapsule,
			) {
				Box(Modifier.size(width = 32.dp, height = 4.dp))
			}
		}
	) {
		Column(
			modifier = Modifier.verticalScroll(rememberScrollState())
		) {
			Form {
				FormRow(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					contentPadding = PaddingValues(12.dp),
					color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
				) {
					Monogram(text = settings.getString("username", ""))

					Column(
						modifier = Modifier.weight(1f)
					) {
						Text(
							text = settings.getString("username", ""),
							style = MaterialTheme.typography.headlineSmall
						)

						Text(
							text = settings.getString("instanceUrl", ""),
							style = MaterialTheme.typography.labelSmall,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}

			Form(bottomPadding = 0.dp) {
				val contentPadding = PaddingValues(18.dp)
				val horizontalArrangement = Arrangement.spacedBy(12.dp)
				val color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)

				FormRow(
					onClick = {
						animateToDismiss()
						backStack.add(Screen.ShareList)
					},
					horizontalArrangement = horizontalArrangement,
					contentPadding = contentPadding,
					color = color
				) {
					Icon(
						imageVector = Icons.Outlined.Share,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(stringResource(Res.string.action_view_shares), Modifier.weight(1f))
				}

				FormRow(
					onClick = {
						sleepTimerSheetOpen = true
					},
					horizontalArrangement = horizontalArrangement,
					contentPadding = contentPadding,
					color = color
				) {
					if (sleepTimerLeft != null) {
						Icon(
							imageVector = Icons.Outlined.Bedtime,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.positive
						)
						Text(
							text = stringResource(
								resource = Res.string.action_sleep_timer_enabled,
								sleepTimerLeft.label()
							),
							color = MaterialTheme.colorScheme.positive,
							modifier = Modifier.weight(1f)
						)
					} else {
						Icon(
							imageVector = Icons.Outlined.Bedtime,
							contentDescription = null,
							tint = MaterialTheme.colorScheme.onSurfaceVariant
						)
						Text(
							text = stringResource(Res.string.action_sleep_timer),
							modifier = Modifier.weight(1f)
						)
					}
				}

				FormRow(
					onClick = {
						animateToDismiss()
						loginViewModel.logout()
						backStack.clear()
						backStack.add(Screen.Login)
					},
					horizontalArrangement = horizontalArrangement,
					contentPadding = contentPadding,
					color = color
				) {
					Icon(
						imageVector = Icons.Outlined.Logout,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(stringResource(Res.string.action_log_out), Modifier.weight(1f))
				}
			}
		}
	}

	if (sleepTimerSheetOpen) {
		SleepTimerSheet(onDismissRequest = { sleepTimerSheetOpen = false })
	}
}

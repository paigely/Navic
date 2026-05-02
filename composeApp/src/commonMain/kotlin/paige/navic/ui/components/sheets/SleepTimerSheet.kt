package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_disable_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalCtx
import paige.navic.managers.SleepTimerManager
import paige.navic.utils.label
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

val durations = listOf(
	5.minutes,
	10.minutes,
	15.minutes,
	30.minutes,
	45.minutes,
	1.hours,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SleepTimerSheet(
	onDismissRequest: (confirmed: Boolean) -> Unit
) {
	val ctx = LocalCtx.current
	val contentPadding = PaddingValues(horizontal = 16.dp)
	val colors = ListItemDefaults.colors(
		containerColor = Color.Transparent,
		trailingIconColor = MaterialTheme.colorScheme.onSurface,
		headlineColor = MaterialTheme.colorScheme.onSurface
	)
	val sleepTimerManager = koinInject<SleepTimerManager>()

	ModalBottomSheet(
		onDismissRequest = { onDismissRequest(false) },
		sheetState = rememberModalBottomSheetState(true),
		contentWindowInsets = { BottomSheetDefaults.modalWindowInsets.add(WindowInsets(
			left = 8.dp,
			right = 8.dp
		)) }
	) {
		Column(
			modifier = Modifier.verticalScroll(rememberScrollState()),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			Text(
				text = stringResource(Res.string.action_sleep_timer),
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.padding(horizontal = 16.dp)
			)

			durations.forEach {
				ListItem(
					content = { Text(it.label()) },
					onClick = {
						ctx.clickSound()
						sleepTimerManager.startTimer(it)
						onDismissRequest(true)
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}

			sleepTimerManager.endTimeStamp?.let {
				ListItem(
					content = { Text(stringResource(Res.string.action_disable_sleep_timer), color = MaterialTheme.colorScheme.error) },
					onClick = {
						ctx.clickSound()
						sleepTimerManager.stopTimer()
						onDismissRequest(true)
					},
					colors = colors,
					contentPadding = contentPadding
				)
			}
		}
	}
}

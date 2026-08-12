package paige.navic.ui.components.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_disable_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer
import navic.composeapp.generated.resources.action_sleep_timer_queue
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.title_sleep_timer_songs
import navic.composeapp.generated.resources.title_sleep_timer_time
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.SleepTimerManager
import paige.navic.domain.manager.SleepTimerMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Bedtime
import paige.navic.icons.outlined.Queue
import paige.navic.icons.outlined.QueuePlayNext
import paige.navic.ui.components.common.OptionCard
import paige.navic.util.core.label
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

val songCounts = listOf(1, 2, 3, 5, 10)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
fun SleepTimerSheet(
	onDismissRequest: (confirmed: Boolean) -> Unit
) {
	val sleepTimerManager = koinInject<SleepTimerManager>()
	val currentMode by sleepTimerManager.mode.collectAsState()

	var selectedTab by remember(currentMode) {
		mutableIntStateOf(
			when (currentMode) {
				is SleepTimerMode.Songs, SleepTimerMode.EndOfQueue -> 1
				else -> 0
			}
		)
	}

	ModalBottomSheet(
		onDismissRequest = { onDismissRequest(false) },
		sheetState = rememberBottomSheetState(
			initialValue = SheetValue.Hidden,
			enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
		),
		contentWindowInsets = {
			BottomSheetDefaults.modalWindowInsets.add(
				WindowInsets(
					left = 8.dp,
					right = 8.dp
				)
			)
		}
	) {
		Column(
			modifier = Modifier
				.verticalScroll(rememberScrollState())
				.padding(bottom = 16.dp),
			verticalArrangement = Arrangement.spacedBy(16.dp)
		) {
			Text(
				text = stringResource(Res.string.action_sleep_timer),
				style = MaterialTheme.typography.titleLarge,
				modifier = Modifier.padding(horizontal = 16.dp)
			)

			PrimaryTabRow(
				selectedTabIndex = selectedTab,
				containerColor = Color.Transparent,
				divider = {}
			) {
				Tab(
					selected = selectedTab == 0,
					onClick = { selectedTab = 0 },
					text = { Text(stringResource(Res.string.title_sleep_timer_time)) }
				)
				Tab(
					selected = selectedTab == 1,
					onClick = { selectedTab = 1 },
					text = { Text(stringResource(Res.string.title_sleep_timer_songs)) }
				)
			}

			if (selectedTab == 0) {
				FlowRow(
					modifier = Modifier.padding(horizontal = 16.dp),
					horizontalArrangement = Arrangement.spacedBy(8.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp),
					maxItemsInEachRow = 3
				) {
					durations.forEach { duration ->
						OptionCard(
							label = duration.label(),
							icon = Icons.Outlined.Bedtime,
							onClick = {
								sleepTimerManager.startTimer(duration)
								onDismissRequest(true)
							},
							modifier = Modifier.weight(1f)
						)
					}
				}
			} else {
				Column(
					modifier = Modifier.padding(horizontal = 16.dp),
					verticalArrangement = Arrangement.spacedBy(8.dp)
				) {
					OptionCard(
						label = stringResource(Res.string.action_sleep_timer_queue),
						icon = Icons.Outlined.QueuePlayNext,
						isActive = currentMode is SleepTimerMode.EndOfQueue,
						onClick = {
							sleepTimerManager.startEndOfQueueTimer()
							onDismissRequest(true)
						},
						modifier = Modifier.fillMaxWidth()
					)

					FlowRow(
						horizontalArrangement = Arrangement.spacedBy(8.dp),
						verticalArrangement = Arrangement.spacedBy(8.dp),
						maxItemsInEachRow = 3
					) {
						songCounts.forEach { count ->
							OptionCard(
								label = pluralStringResource(Res.plurals.count_songs, count, count),
								icon = Icons.Outlined.Queue,
								isActive = (currentMode as? SleepTimerMode.Songs)?.remaining == count,
								onClick = {
									sleepTimerManager.startSongsTimer(count)
									onDismissRequest(true)
								},
								modifier = Modifier.weight(1f)
							)
						}
					}
				}
			}

			if (currentMode !is SleepTimerMode.Disabled) {
				Spacer(Modifier.height(8.dp))
				ListItem(
					onClick = {
						sleepTimerManager.stopTimer()
						onDismissRequest(true)
					},
					colors = ListItemDefaults.colors(containerColor = Color.Transparent)
				) {
					Text(
						stringResource(Res.string.action_disable_sleep_timer),
						color = MaterialTheme.colorScheme.error,
						modifier = Modifier.fillMaxWidth(),
						textAlign = TextAlign.Center
					)
				}
			}
		}
	}
}

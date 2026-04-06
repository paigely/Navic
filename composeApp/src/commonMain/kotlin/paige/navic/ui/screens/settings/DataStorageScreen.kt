package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_clear_downloads
import navic.composeapp.generated.resources.action_clear_image_cache
import navic.composeapp.generated.resources.action_clear_pending_actions
import navic.composeapp.generated.resources.action_rebuild_database
import navic.composeapp.generated.resources.action_trigger_sync
import navic.composeapp.generated.resources.count_songs
import navic.composeapp.generated.resources.info_status_idle
import navic.composeapp.generated.resources.info_sync_date_format
import navic.composeapp.generated.resources.info_sync_hours_ago
import navic.composeapp.generated.resources.info_sync_just_now
import navic.composeapp.generated.resources.info_sync_mins_ago
import navic.composeapp.generated.resources.info_sync_never
import navic.composeapp.generated.resources.option_downloaded_songs
import navic.composeapp.generated.resources.option_image_cache_size
import navic.composeapp.generated.resources.option_last_sync
import navic.composeapp.generated.resources.option_live_status
import navic.composeapp.generated.resources.option_pending_actions
import navic.composeapp.generated.resources.subtitle_pending_actions
import navic.composeapp.generated.resources.subtitle_rebuild_database
import navic.composeapp.generated.resources.subtitle_trigger_sync
import navic.composeapp.generated.resources.title_cache_management
import navic.composeapp.generated.resources.title_danger_zone
import navic.composeapp.generated.resources.title_data_storage
import navic.composeapp.generated.resources.title_sync_control
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.viewmodels.SettingsDataStorageViewModel
import paige.navic.utils.fadeFromTop
import kotlin.time.Clock
import kotlin.time.Instant

@Composable
fun SettingsDataStorageScreen() {
	val viewModel = koinViewModel<SettingsDataStorageViewModel>()

	val ctx = LocalCtx.current
	val scope = rememberCoroutineScope()
	val platformContext = LocalPlatformContext.current
	val imageLoader = SingletonImageLoader.get(platformContext)

	val syncState by viewModel.syncState.collectAsStateWithLifecycle()
	val pendingActionCount by viewModel.pendingActionCount.collectAsStateWithLifecycle()
	val downloadCount by viewModel.downloadCount.collectAsStateWithLifecycle(0)
	val downloadSize by viewModel.downloadSize.collectAsStateWithLifecycle(0L)

	var imageCacheSizeMb by remember { mutableStateOf("Calculating...") }

	val downloadsSizeMb = remember(downloadSize) {
		val mb = downloadSize.toDouble() / (1024 * 1024)
		if (mb > 1024) {
			val gb = mb / 1024
			" // ${(gb * 100).toInt() / 100.0} GB"
		} else {
			" // ${mb.toInt()} MB"
		}
	}

	val smoothSyncProgress by animateFloatAsState(
		if (syncState.isSyncing) syncState.progress else 0f,
		animationSpec = tween(
			durationMillis = 250,
			easing = EaseOut
		)
	)

	LaunchedEffect(Unit) {
		withContext(Dispatchers.IO) {
			val sizeBytes = imageLoader.diskCache?.size ?: 0L
			imageCacheSizeMb = "${sizeBytes / (1024 * 1024)} MB"
		}
	}

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(Res.string.title_data_storage)) },
				hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		},
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 32.dp)
					.fadeFromTop()
			) {
				FormTitle(stringResource(Res.string.title_sync_control))
				Form {
					FormRow {
						Column(Modifier.fillMaxWidth()) {
							Column {
								Text(stringResource(Res.string.option_live_status))
								Text(
									text = syncState.message.ifEmpty { stringResource(Res.string.info_status_idle) },
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							AnimatedVisibility(
								syncState.isSyncing,
								enter = fadeIn() + expandVertically(clip = false),
								exit = fadeOut() + shrinkVertically(clip = false)
							) {
								LinearProgressIndicator(
									progress = {
										if (syncState.isSyncing)
											1f
										else smoothSyncProgress.coerceIn(0f, 1f)
									},
									modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
								)
							}
						}
					}

					FormRow(onClick = { viewModel.triggerManualSync() }) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.action_trigger_sync))
							Text(
								stringResource(Res.string.subtitle_trigger_sync),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					FormRow {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_last_sync))
							Text(
								text = if (Settings.shared.lastFullSyncTime == 0L) {
									stringResource(Res.string.info_sync_never)
								} else {
									Instant.fromEpochMilliseconds(
										Settings.shared.lastFullSyncTime
									).toRelativeString(
										justNow = stringResource(Res.string.info_sync_just_now),
										minsAgo = stringResource(Res.string.info_sync_mins_ago),
										hoursAgo = stringResource(Res.string.info_sync_hours_ago),
										dateFormat = stringResource(Res.string.info_sync_date_format)
									)
								},
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}

				FormTitle(stringResource(Res.string.title_cache_management))
				Form {
					FormRow {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_pending_actions))
							Text(
								stringResource(Res.string.subtitle_pending_actions, pendingActionCount),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					FormRow {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_downloaded_songs))
							Text(
								pluralStringResource(Res.plurals.count_songs, downloadCount, downloadCount)
									+ downloadsSizeMb,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					FormRow {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_image_cache_size))
							Text(
								imageCacheSizeMb,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}
				}

				FormTitle(stringResource(Res.string.title_danger_zone))
				Form {
					FormRow(
						onClick = {
							scope.launch(Dispatchers.IO) {
								imageLoader.diskCache?.clear()
								imageLoader.memoryCache?.clear()
								imageCacheSizeMb = "0 MB"
							}
						}
					) {
						Text(
							stringResource(Res.string.action_clear_image_cache),
							color = MaterialTheme.colorScheme.error,
							modifier = Modifier.weight(1f)
						)
					}

					FormRow(onClick = { viewModel.removeAllActions() }) {
						Text(
							stringResource(Res.string.action_clear_pending_actions),
							color = MaterialTheme.colorScheme.error
						)
					}

					FormRow(onClick = { viewModel.clearAllDownloads() }) {
						Text(
							stringResource(Res.string.action_clear_downloads),
							color = MaterialTheme.colorScheme.error
						)
					}

					FormRow(onClick = { viewModel.rebuildDatabase() }) {
						Column(Modifier.weight(1f)) {
							Text(
								stringResource(Res.string.action_rebuild_database),
								color = MaterialTheme.colorScheme.error
							)
							Text(
								stringResource(Res.string.subtitle_rebuild_database),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
							)
						}
					}
				}
			}
		}
	}
}

fun Instant.toRelativeString(
	justNow: String,
	minsAgo: String,
	hoursAgo: String,
	dateFormat: String
): String {
	val now = Clock.System.now()
	val diff = now - this
	val seconds = diff.inWholeSeconds

	return when {
		seconds < 60 -> justNow
		seconds < 3600 -> minsAgo.replace($$"%1$d", (seconds / 60).toString())
		seconds < 86400 -> hoursAgo.replace($$"%1$d", (seconds / 3600).toString())
		else -> {
			val date = this.toLocalDateTime(TimeZone.currentSystemDefault())
			val monthName = date.month.name.lowercase().take(3)
			dateFormat
				.replace($$"%1$d", date.day.toString())
				.replace($$"%1$s", monthName)
		}
	}
}
package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_bitrate_default_zero
import navic.composeapp.generated.resources.info_in_use
import navic.composeapp.generated.resources.info_streaming_quality
import navic.composeapp.generated.resources.option_enable_custom_bitrates
import navic.composeapp.generated.resources.option_max_bitrate_cellular
import navic.composeapp.generated.resources.option_max_bitrate_wifi
import navic.composeapp.generated.resources.subtitle_max_bitrates
import navic.composeapp.generated.resources.title_advanced
import navic.composeapp.generated.resources.title_cellular
import navic.composeapp.generated.resources.title_streaming_quality
import navic.composeapp.generated.resources.title_wifi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.StreamingQuality
import paige.navic.data.models.settings.enums.description
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Info
import paige.navic.managers.ConnectivityManager
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar

@Composable
fun SettingsStreamingQualityScreen() {
	val ctx = LocalCtx.current
	val connectivityManager = koinInject<ConnectivityManager>()
	val isOnline by connectivityManager.isOnline.collectAsStateWithLifecycle()
	val isCellular by connectivityManager.isCellular.collectAsStateWithLifecycle()

	var isAdvancedActive by remember { mutableStateOf(Settings.shared.isAdvancedTranscodingActive) }

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_streaming_quality)) },
				hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		},
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp, bottom = 32.dp)
			) {
				AnimatedVisibility(visible = !isAdvancedActive) {
					Column {
						FormTitle(buildString {
							append(stringResource(Res.string.title_wifi))
							if (isOnline && !isCellular) {
								append(' ' + stringResource(Res.string.info_in_use))
							}
						})
						Form(Modifier.selectableGroup()) {
							RadioButtons(
								value = Settings.shared.streamingQualityWifi,
								onChangeValue = { Settings.shared.streamingQualityWifi = it }
							)
						}

						FormTitle(buildString {
							append(stringResource(Res.string.title_cellular))
							if (isOnline && isCellular) {
								append(' ' + stringResource(Res.string.info_in_use))
							}
						})
						Form(Modifier.selectableGroup()) {
							RadioButtons(
								value = Settings.shared.streamingQualityCellular,
								onChangeValue = { Settings.shared.streamingQualityCellular = it }
							)
						}
					}
				}

				Spacer(Modifier.height(16.dp))
				FormTitle(stringResource(Res.string.title_advanced))

				Form {
					val interactionSource = remember { MutableInteractionSource() }
					FormRow(
						modifier = Modifier.clickable(
							interactionSource = interactionSource,
							indication = null,
							onClick = {
								isAdvancedActive = !isAdvancedActive
								Settings.shared.isAdvancedTranscodingActive = isAdvancedActive
							}
						),
						horizontalArrangement = Arrangement.SpaceBetween,
						contentPadding = PaddingValues(16.dp)
					) {
						Text(
							text = stringResource(Res.string.option_enable_custom_bitrates),
							style = MaterialTheme.typography.bodyLarge
						)
						Switch(
							checked = isAdvancedActive,
							onCheckedChange = {
								isAdvancedActive = it
								Settings.shared.isAdvancedTranscodingActive = it
							}
						)
					}

					AnimatedVisibility(visible = isAdvancedActive) {
						Column(Modifier.padding(16.dp)) {
							Text(
								text = stringResource(Res.string.subtitle_max_bitrates),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)

							Spacer(Modifier.height(16.dp))

							var wifiInput by remember {
								val current = Settings.shared.customMaxBitrateWifi
								mutableStateOf(if (current > 0) current.toString() else "")
							}

							OutlinedTextField(
								value = wifiInput,
								onValueChange = { newValue ->
									if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
										wifiInput = newValue
										Settings.shared.customMaxBitrateWifi = newValue.toIntOrNull() ?: 0
									}
								},
								label = { Text(stringResource(Res.string.option_max_bitrate_wifi)) },
								placeholder = { Text("0") },
								supportingText = {
									Text(stringResource(Res.string.info_bitrate_default_zero))
								},
								keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
								modifier = Modifier.fillMaxWidth(),
								singleLine = true
							)

							Spacer(Modifier.height(16.dp))

							var cellularInput by remember {
								val current = Settings.shared.customMaxBitrateCellular
								mutableStateOf(if (current > 0) current.toString() else "")
							}

							OutlinedTextField(
								value = cellularInput,
								onValueChange = { newValue ->
									if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
										cellularInput = newValue
										Settings.shared.customMaxBitrateCellular = newValue.toIntOrNull() ?: 0
									}
								},
								label = { Text(stringResource(Res.string.option_max_bitrate_cellular)) },
								placeholder = { Text("0") },
								supportingText = {
									Text(stringResource(Res.string.info_bitrate_default_zero))
								},
								keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
								modifier = Modifier.fillMaxWidth(),
								singleLine = true
							)
						}
					}
				}

				Spacer(Modifier.height(24.dp))
				Row(
					modifier = Modifier.padding(horizontal = 8.dp),
					horizontalArrangement = Arrangement.spacedBy(16.dp)
				) {
					Icon(
						Icons.Outlined.Info,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onSurfaceVariant
					)
					Text(
						stringResource(Res.string.info_streaming_quality),
						color = MaterialTheme.colorScheme.onSurfaceVariant,
						style = MaterialTheme.typography.bodyMedium
					)
				}
			}
		}
	}
}

@Composable
private fun RadioButtons(
	value: StreamingQuality,
	onChangeValue: (StreamingQuality) -> Unit
) {
	StreamingQuality.entries.forEach { quality ->
		val interactionSource = remember { MutableInteractionSource() }

		FormRow(
			modifier = Modifier.selectable(
				selected = value == quality,
				interactionSource = interactionSource,
				onClick = { onChangeValue(quality) },
				role = Role.RadioButton
			),
			horizontalArrangement = Arrangement.spacedBy(14.dp),
			interactionSource = interactionSource,
			contentPadding = PaddingValues(16.dp)
		) {
			RadioButton(
				selected = value == quality,
				onClick = null
			)

			Column(Modifier.weight(1f)) {
				Text(stringResource(quality.displayName))

				quality.description()?.let { description ->
					AnimatedVisibility(
						visible = value == quality
					) {
						Text(
							text = description,
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
				}
			}
		}
	}
}

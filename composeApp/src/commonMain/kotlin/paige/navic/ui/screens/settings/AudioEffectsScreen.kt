package paige.navic.ui.screens.settings

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_audio_offload
import navic.composeapp.generated.resources.option_dynamic_replaygain_tip
import navic.composeapp.generated.resources.option_equaliser
import navic.composeapp.generated.resources.option_gapless_playback
import navic.composeapp.generated.resources.option_preamp_tip
import navic.composeapp.generated.resources.option_preamp_with_rg
import navic.composeapp.generated.resources.option_preamp_without_rg
import navic.composeapp.generated.resources.option_replaygain_mode
import navic.composeapp.generated.resources.option_title_preamp
import navic.composeapp.generated.resources.subtitle_audio_offload
import navic.composeapp.generated.resources.subtitle_equaliser
import navic.composeapp.generated.resources.subtitle_equaliser_disabled
import navic.composeapp.generated.resources.subtitle_gapless_playback
import navic.composeapp.generated.resources.title_audio_effects
import navic.composeapp.generated.resources.title_playback
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalNavStack
import paige.navic.domain.manager.AudioGainManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.ReplayGainMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.icons.outlined.Info
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import kotlin.math.absoluteValue
import kotlin.math.round

@Composable
fun AudioEffectsScreen() {
	val preferenceManager = koinInject<PreferenceManager>()
	val audioGainManager = koinInject<AudioGainManager>()
	val backStack = LocalNavStack.current

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(Res.string.title_audio_effects)) }
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
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
			) {

				FormTitle(stringResource(Res.string.title_playback))
				Form {
					FormRow(
						onClick = dropUnlessResumed { backStack.add(Screen.Settings.Equaliser) },
						horizontalArrangement = Arrangement.Start,
						enabled = !preferenceManager.audioOffload
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_equaliser))
							Text(
								text = stringResource(
									if (!preferenceManager.audioOffload)
										Res.string.subtitle_equaliser
									else Res.string.subtitle_equaliser_disabled
								),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						Icon(Icons.Outlined.ChevronForward, null)
					}
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_gapless_playback)) },
						subtitle = { Text(stringResource(Res.string.subtitle_gapless_playback)) },
						value = preferenceManager.gaplessPlayback,
						onSetValue = { preferenceManager.gaplessPlayback = it }
					)
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_audio_offload)) },
						subtitle = { Text(stringResource(Res.string.subtitle_audio_offload)) },
						value = preferenceManager.audioOffload,
						onSetValue = { preferenceManager.audioOffload = it }
					)
				}

				FormTitle(stringResource(Res.string.option_replaygain_mode))
				Form(Modifier.selectableGroup()) {
					ReplayGainMode.entries.forEach { mode ->
						val interactionSource = remember { MutableInteractionSource() }

						FormRow(
							modifier = Modifier.selectable(
								selected = preferenceManager.replayGainMode == mode,
								interactionSource = interactionSource,
								onClick = {
									preferenceManager.replayGainMode = mode
									audioGainManager.applyGainMode(mode)
								},
								role = Role.RadioButton
							),
							horizontalArrangement = Arrangement.spacedBy(14.dp),
							contentPadding = PaddingValues(16.dp)
						) {
							RadioButton(
								selected = preferenceManager.replayGainMode == mode,
								onClick = null
							)

							Text(stringResource(mode.displayName))
						}
					}
				}

				InformationTip(stringResource(Res.string.option_dynamic_replaygain_tip))

				FormTitle(stringResource(Res.string.option_title_preamp))
				Form {
					FormRow {
						Column(Modifier.fillMaxWidth()) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(stringResource(Res.string.option_preamp_with_rg))
								Text(
									preferenceManager.rgAmpGain.decibelsToHuman(),
									fontFamily = FontFamily.Monospace,
									fontWeight = FontWeight(400),
									fontSize = 13.sp,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							Slider(
								value = preferenceManager.rgAmpGain,
								onValueChange = {
									preferenceManager.rgAmpGain = it.round(1)
									audioGainManager.setAmplifierValues(
										it,
										preferenceManager.ampGain
									)
								},
								valueRange = -12f..12f,
							)
						}
					}
					FormRow {
						Column(Modifier.fillMaxWidth()) {
							Row(
								modifier = Modifier.fillMaxWidth(),
								horizontalArrangement = Arrangement.SpaceBetween
							) {
								Text(stringResource(Res.string.option_preamp_without_rg))
								Text(
									preferenceManager.ampGain.decibelsToHuman(),
									fontFamily = FontFamily.Monospace,
									fontWeight = FontWeight(400),
									fontSize = 13.sp,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							Slider(
								value = preferenceManager.ampGain,
								onValueChange = {
									preferenceManager.ampGain = it.round(1)
									audioGainManager.setAmplifierValues(
										preferenceManager.rgAmpGain,
										it
									)
								},
								valueRange = -12f..12f,
							)
						}
					}
				}

				InformationTip(stringResource(Res.string.option_preamp_tip))
			}
		}
	}
}


private fun Float.round(decimals: Int): Float {
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return (round(this * multiplier) / multiplier).toFloat()
}

private fun Float.decibelsToHuman(): String {
	val decibels = this.round(1)
	return buildString {
		if (decibels < 0) {
			append("-")
		} else if (decibels > 0) {
			append("+")
		}
		append("${decibels.absoluteValue}db")
	}
}

@Composable
private fun InformationTip(text: String) {
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
			text = text,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			style = MaterialTheme.typography.bodyMedium
		)
	}
	Spacer(Modifier.height(24.dp))
}

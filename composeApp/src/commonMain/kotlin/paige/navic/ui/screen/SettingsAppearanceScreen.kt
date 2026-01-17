package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import dev.burnoo.compose.remembersetting.rememberFloatSetting
import dev.zt64.compose.pipette.CircularColorPicker
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_accent_colour
import navic.composeapp.generated.resources.option_always_show_seekbar
import navic.composeapp.generated.resources.option_cover_art_rounding
import navic.composeapp.generated.resources.option_cover_art_size
import navic.composeapp.generated.resources.option_dynamic_colour
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_system_font
import navic.composeapp.generated.resources.subtitle_system_font
import navic.composeapp.generated.resources.switch_off
import navic.composeapp.generated.resources.switch_on
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.dialog.NavtabsDialog
import paige.navic.ui.theme.mapleMono

@Composable
fun SettingsAppearanceScreen() {
	var showNavtabsDialog by rememberSaveable { mutableStateOf(false) }
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)
	var dynamicColour by rememberBooleanSetting("dynamicColour", true)
	var accentColourH by rememberFloatSetting("accentColourH", 0f)
	var accentColourS by rememberFloatSetting("accentColourS", 0f)
	var accentColourV by rememberFloatSetting("accentColourV", 1f)
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)
	var artGridRounding by rememberFloatSetting("artGridRounding", 16f)
	var artGridSize by rememberFloatSetting("artGridSize", 150f)
	var alwaysShowSeekbar by rememberBooleanSetting("alwaysShowSeekbar", true)
	CompositionLocalProvider(
		LocalMinimumInteractiveComponentSize provides 0.dp
	) {
		Column(
			Modifier
				.verticalScroll(rememberScrollState())
				.padding(12.dp)
				.padding(bottom = 117.9.dp)
		) {
			Form {
				FormRow {
					Column {
						Text(stringResource(Res.string.option_system_font))
						Text(
							stringResource(Res.string.subtitle_system_font),
							style = MaterialTheme.typography.bodyMedium,
							color = MaterialTheme.colorScheme.onSurfaceVariant
						)
					}
					SettingSwitch(
						checked = useSystemFont,
						onCheckedChange = { useSystemFont = it }
					)
				}
				FormRow {
					Text(stringResource(Res.string.option_dynamic_colour))
					SettingSwitch(
						checked = dynamicColour,
						onCheckedChange = { dynamicColour = it }
					)
				}
				if (!dynamicColour) {
					var expanded by remember { mutableStateOf(false) }
					FormRow {
						Text(stringResource(Res.string.option_accent_colour))
						Box {
							Box(
								Modifier
									.background(
										HsvColor(
											accentColourH, accentColourS, accentColourV
										).toColor(), CircleShape
									)
									.size(40.dp)
									.clickable {
										expanded = true
									}
							)
							Dropdown(
								expanded = expanded,
								onDismissRequest = { expanded = false }
							) {
								FormRow(
									color = MaterialTheme.colorScheme.surfaceContainerHigh,
									horizontalArrangement = Arrangement.Center
								) {
									CircularColorPicker(
										color = {
											HsvColor(
												accentColourH,
												accentColourS,
												accentColourV
											)
										},
										onColorChange = {
											accentColourH = it.hue
											accentColourS = it.saturation
											accentColourV = it.value
										}
									)
								}
							}
						}
					}
				}
			}
			Form {
				FormRow {
					Column(Modifier.fillMaxWidth()) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(stringResource(Res.string.option_cover_art_rounding))
							Text(
								"$artGridRounding",
								fontFamily = mapleMono(),
								fontWeight = FontWeight(400),
								fontSize = 13.sp,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
							)
						}
						Slider(
							value = artGridRounding,
							onValueChange = {
								artGridRounding = it
							},
							valueRange = 0f..64f,
							steps = 3,
						)
					}
				}
				FormRow {
					Column(Modifier.fillMaxWidth()) {
						Row(
							modifier = Modifier.fillMaxWidth(),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(stringResource(Res.string.option_cover_art_size))
							Text(
								"$artGridSize",
								fontFamily = mapleMono(),
								fontWeight = FontWeight(400),
								fontSize = 13.sp,
								color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
							)
						}
						Slider(
							value = artGridSize,
							onValueChange = {
								artGridSize = it
							},
							valueRange = 50f..500f,
							steps = 8,
						)
					}
				}
			}
			Form {
				FormRow {
					Text(stringResource(Res.string.option_short_navigation_bar))
					SettingSwitch(
						checked = useShortNavbar,
						onCheckedChange = { useShortNavbar = it }
					)
				}
				FormRow {
					Text(stringResource(Res.string.option_always_show_seekbar))
					SettingSwitch(
						checked = alwaysShowSeekbar,
						onCheckedChange = { alwaysShowSeekbar = it }
					)
				}
				FormRow(
					onClick = {
						showNavtabsDialog = true
					}
				) {
					Text(stringResource(Res.string.option_navbar_tab_positions))
				}
			}
		}
	}
	NavtabsDialog(
		presented = showNavtabsDialog,
		onDismissRequest = { showNavtabsDialog = false }
	)
}

@Composable
private fun SettingSwitch(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Switch(
		checked = checked,
		onCheckedChange = onCheckedChange,
		thumbContent = {
			Icon(
				vectorResource(
					if (checked) Res.drawable.switch_on else Res.drawable.switch_off
				),
				contentDescription = null,
				modifier = Modifier.size(19.dp)
			)
		}
	)
}
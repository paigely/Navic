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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import dev.burnoo.compose.remembersetting.rememberIntSetting
import dev.zt64.compose.pipette.CircularColorPicker
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_accent_colour
import navic.composeapp.generated.resources.option_always_show_seekbar
import navic.composeapp.generated.resources.option_cover_art_rounding
import navic.composeapp.generated.resources.option_cover_art_size
import navic.composeapp.generated.resources.option_dynamic_colour
import navic.composeapp.generated.resources.option_grid_items_per_row
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_system_font
import navic.composeapp.generated.resources.subtitle_grid_items_per_row
import navic.composeapp.generated.resources.subtitle_system_font
import navic.composeapp.generated.resources.title_appearance
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.ui.component.common.Dropdown
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.common.SettingSwitch
import paige.navic.ui.component.common.Stepper
import paige.navic.ui.component.dialog.NavtabsDialog
import paige.navic.ui.component.layout.NestedTopBar
import paige.navic.ui.theme.mapleMono

@Composable
fun SettingsAppearanceScreen() {
	val ctx = LocalCtx.current
	var showNavtabsDialog by rememberSaveable { mutableStateOf(false) }
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)
	var dynamicColour by rememberBooleanSetting("dynamicColour", true)
	var accentColourH by rememberFloatSetting("accentColourH", 0f)
	var accentColourS by rememberFloatSetting("accentColourS", 0f)
	var accentColourV by rememberFloatSetting("accentColourV", 1f)
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)
	var artGridRounding by rememberFloatSetting("artGridRounding", 16f)
	var artGridItemsPerRow by rememberIntSetting("artGridItemsPerRow", 2)
	var artGridItemSize by rememberFloatSetting("artGridItemSize", 150f)
	var alwaysShowSeekbar by rememberBooleanSetting("alwaysShowSeekbar", true)
	val hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_appearance)) },
			hideBack = hideBack
		) }
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.padding(innerPadding)
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
									color = MaterialTheme.colorScheme.onSurfaceVariant,
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
						if (ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {
							Column {
								Text(stringResource(Res.string.option_grid_items_per_row) + ": $artGridItemsPerRow")
								Text(
									stringResource(Res.string.subtitle_grid_items_per_row),
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							Stepper(
								value = artGridItemsPerRow,
								onValueChange = { artGridItemsPerRow = it },
								minValue = 1,
								maxValue = 32
							)
						} else {
							Column(Modifier.fillMaxWidth()) {
								Row(
									modifier = Modifier.fillMaxWidth(),
									horizontalArrangement = Arrangement.SpaceBetween
								) {
									Text(stringResource(Res.string.option_cover_art_size))
									Text(
										"$artGridItemSize",
										fontFamily = mapleMono(),
										fontWeight = FontWeight(400),
										fontSize = 13.sp,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
								Slider(
									value = artGridItemSize,
									onValueChange = {
										artGridItemSize = it
									},
									valueRange = 50f..500f,
									steps = 8,
								)
							}
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
	}
	NavtabsDialog(
		presented = showNavtabsDialog,
		onDismissRequest = { showNavtabsDialog = false }
	)
}

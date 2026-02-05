package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
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
import dev.zt64.compose.pipette.CircularColorPicker
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_accent_colour
import navic.composeapp.generated.resources.option_cover_art_rounding
import navic.composeapp.generated.resources.option_cover_art_size
import navic.composeapp.generated.resources.option_dynamic_colour
import navic.composeapp.generated.resources.option_grid_items_per_row
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_system_font
import navic.composeapp.generated.resources.option_use_marquee_text
import navic.composeapp.generated.resources.option_use_wavy_slider
import navic.composeapp.generated.resources.subtitle_grid_items_per_row
import navic.composeapp.generated.resources.subtitle_system_font
import navic.composeapp.generated.resources.subtitle_use_marquee_text
import navic.composeapp.generated.resources.title_appearance
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.data.model.Settings
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
	Scaffold(
		topBar = { NestedTopBar(
			{ Text(stringResource(Res.string.title_appearance)) },
			hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
		) },
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
							checked = Settings.shared.useSystemFont,
							onCheckedChange = { Settings.shared.useSystemFont = it }
						)
					}
					FormRow {
						Text(stringResource(Res.string.option_dynamic_colour))
						SettingSwitch(
							checked = Settings.shared.dynamicColour,
							onCheckedChange = { Settings.shared.dynamicColour = it }
						)
					}
					if (!Settings.shared.dynamicColour) {
						var expanded by remember { mutableStateOf(false) }
						FormRow {
							Text(stringResource(Res.string.option_accent_colour))
							Box {
								Box(
									Modifier
										.background(
											HsvColor(
												Settings.shared.accentColourH, Settings.shared.accentColourS, Settings.shared.accentColourV
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
													Settings.shared.accentColourH,
													Settings.shared.accentColourS,
													Settings.shared.accentColourV
												)
											},
											onColorChange = {
												Settings.shared.accentColourH = it.hue
												Settings.shared.accentColourS = it.saturation
												Settings.shared.accentColourV = it.value
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
									"${Settings.shared.artGridRounding}",
									fontFamily = mapleMono(),
									fontWeight = FontWeight(400),
									fontSize = 13.sp,
									color = MaterialTheme.colorScheme.onSurfaceVariant,
								)
							}
							Slider(
								value = Settings.shared.artGridRounding,
								onValueChange = {
									Settings.shared.artGridRounding = it
								},
								valueRange = 0f..64f,
								steps = 3,
							)
						}
					}
					FormRow {
						if (ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {
							Column(
								modifier = Modifier
									.weight(1f, fill = true)
									.padding(end = 8.dp)
							) {
								Text(stringResource(Res.string.option_grid_items_per_row) + ": ${Settings.shared.artGridItemsPerRow}")
								Text(
									stringResource(Res.string.subtitle_grid_items_per_row),
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}
							Stepper(
								value = Settings.shared.artGridItemsPerRow,
								onValueChange = { Settings.shared.artGridItemsPerRow = it },
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
										"${Settings.shared.artGridItemSize}",
										fontFamily = mapleMono(),
										fontWeight = FontWeight(400),
										fontSize = 13.sp,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
								Slider(
									value = Settings.shared.artGridItemSize,
									onValueChange = {
										Settings.shared.artGridItemSize = it
									},
									valueRange = 50f..500f,
									steps = 8,
								)
							}
						}
					}
					FormRow {
						Column {
							Text(stringResource(Res.string.option_use_marquee_text))
							Text(
								stringResource(Res.string.subtitle_use_marquee_text),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
						SettingSwitch(
							checked = Settings.shared.useMarquee,
							onCheckedChange = { Settings.shared.useMarquee = it }
						)
					}
				}
				Form {
					FormRow {
						Text(stringResource(Res.string.option_short_navigation_bar))
						SettingSwitch(
							checked = Settings.shared.useShortNavbar,
							onCheckedChange = { Settings.shared.useShortNavbar = it }
						)
					}
					FormRow {
						Text("Detached bottom bar")
						SettingSwitch(
							checked = Settings.shared.detachedBar,
							onCheckedChange = { Settings.shared.detachedBar = it }
						)
					}
					FormRow {
						Text(stringResource(Res.string.option_use_wavy_slider))
						SettingSwitch(
							checked = Settings.shared.useWavySlider,
							onCheckedChange = { Settings.shared.useWavySlider = it }
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
				Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
			}
		}
	}
	NavtabsDialog(
		presented = showNavtabsDialog,
		onDismissRequest = { showNavtabsDialog = false }
	)
}

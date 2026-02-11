package paige.navic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.capsule.ContinuousRoundedRectangle
import dev.zt64.compose.pipette.CircularColorPicker
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_accent_colour
import navic.composeapp.generated.resources.option_alphabetical_scroll
import navic.composeapp.generated.resources.option_artwork_shape
import navic.composeapp.generated.resources.option_cover_art_size
import navic.composeapp.generated.resources.option_dynamic_colour
import navic.composeapp.generated.resources.option_grid_items_per_row
import navic.composeapp.generated.resources.option_marquee_duration
import navic.composeapp.generated.resources.option_system_font
import navic.composeapp.generated.resources.option_use_marquee_text
import navic.composeapp.generated.resources.subtitle_dynamic_colour
import navic.composeapp.generated.resources.subtitle_grid_items_per_row
import navic.composeapp.generated.resources.subtitle_system_font
import navic.composeapp.generated.resources.subtitle_use_marquee_text
import navic.composeapp.generated.resources.title_appearance
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalCtx
import paige.navic.data.models.Settings
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.dialogs.ArtworkShapeDialog
import paige.navic.ui.components.dialogs.GridSizeDialog
import paige.navic.ui.components.dialogs.GridSizePreview
import paige.navic.ui.components.dialogs.MarqueeSpeedDialog
import paige.navic.ui.components.dialogs.Shapes
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.settings.SettingCollapsibleRow
import paige.navic.ui.components.settings.SettingSwitchRow
import paige.navic.ui.theme.mapleMono

@Composable
fun SettingsAppearanceScreen() {
	val ctx = LocalCtx.current
	var showArtworkShapeDialog by rememberSaveable { mutableStateOf(false) }

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
					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_system_font)) },
						subtitle = { Text(stringResource(Res.string.subtitle_system_font)) },
						value = Settings.shared.useSystemFont,
						onSetValue = { Settings.shared.useSystemFont = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_dynamic_colour)) },
						subtitle = { Text(stringResource(Res.string.subtitle_dynamic_colour)) },
						value = Settings.shared.dynamicColour,
						onSetValue = { Settings.shared.dynamicColour = it }
					)

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
					FormRow(
						onClick = {
							showArtworkShapeDialog = true
						}
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_artwork_shape))
							Text(
								Shapes.firstOrNull { it.second == Settings.shared.artGridRounding }?.first
									?: Shapes[0].first,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}

						val shape =
							ContinuousRoundedRectangle(Settings.shared.artGridRounding.dp / 1.5f)
						Box(
							modifier = Modifier
								.size(48.dp)
								.clip(shape)
								.background(MaterialTheme.colorScheme.onPrimary)
								.border(2.dp, MaterialTheme.colorScheme.primary, shape)
						)
					}

					var presented by remember { mutableStateOf(false) }
					val onClick = { presented = true }
					FormRow(
						onClick = if (ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact)
							onClick
						else null
					) {
						if (ctx.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {

							Column(Modifier.weight(1f)) {
								Text(stringResource(Res.string.option_grid_items_per_row))
								Text(
									stringResource(Res.string.subtitle_grid_items_per_row),
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}

							GridSizePreview(Settings.shared.gridSize.value)

							GridSizeDialog(
								presented = presented,
								onDismissRequest = { presented = false }
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
				}

				Form {
					var marqueeSpeedPresented by remember { mutableStateOf(false) }

					SettingCollapsibleRow(
						title = { Text(stringResource(Res.string.option_use_marquee_text)) },
						subtitle = { Text(stringResource(Res.string.subtitle_use_marquee_text)) },
						value = Settings.shared.useMarquee,
						onSetValue = { Settings.shared.useMarquee = it }
					) {
						Row(
							modifier = Modifier.fillMaxWidth().clickable {
								marqueeSpeedPresented = true
							},
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							Text(stringResource(Res.string.option_marquee_duration))
							Text(Settings.shared.marqueeSpeed.name)
						}
					}

					MarqueeSpeedDialog(
						presented = marqueeSpeedPresented,
						onDismissRequest = { marqueeSpeedPresented = false }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_alphabetical_scroll)) },
						value = Settings.shared.alphabeticalScroll,
						onSetValue = { Settings.shared.alphabeticalScroll = it }
					)
				}
				Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
			}
		}
		ArtworkShapeDialog(
			presented = showArtworkShapeDialog,
			onDismissRequest = { showArtworkShapeDialog = false }
		)
	}
}

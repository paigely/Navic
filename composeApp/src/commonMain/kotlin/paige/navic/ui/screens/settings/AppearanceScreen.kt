package paige.navic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.dropUnlessResumed
import dev.zt64.compose.pipette.HsvColor
import dev.zt64.compose.pipette.RingColorPicker
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_accent_colour
import navic.composeapp.generated.resources.option_alphabetical_scroll
import navic.composeapp.generated.resources.option_animation_style
import navic.composeapp.generated.resources.option_artwork_shape
import navic.composeapp.generated.resources.option_choose_theme
import navic.composeapp.generated.resources.option_cover_art_size
import navic.composeapp.generated.resources.option_grid_items_per_row
import navic.composeapp.generated.resources.option_use_marquee_text
import navic.composeapp.generated.resources.title_appearance
import navic.composeapp.generated.resources.title_choose_font
import navic.composeapp.generated.resources.title_layout
import navic.composeapp.generated.resources.title_miscellaneous
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.LocalNavStack
import paige.navic.LocalPlatformContext
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.AnimationStyle
import paige.navic.domain.models.settings.MarqueeSpeed
import paige.navic.domain.models.settings.Theme
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.navigation.Screen
import paige.navic.ui.screens.settings.components.SettingSelectionRow
import paige.navic.ui.screens.settings.components.SettingSwitchRow
import paige.navic.ui.screens.settings.dialogs.ArtworkShapeDialog
import paige.navic.ui.screens.settings.dialogs.GridSizeDialog
import paige.navic.ui.screens.settings.dialogs.GridSizePreview
import paige.navic.ui.screens.settings.dialogs.ThemeDialog

@Composable
fun SettingsAppearanceScreen() {
	val platformContext = LocalPlatformContext.current
	val backStack = LocalNavStack.current
	var showArtworkShapeDialog by rememberSaveable { mutableStateOf(false) }
	val preferenceManager = koinInject<PreferenceManager>()

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.title_appearance)) },
				hideBack = platformContext.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
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
				Form {
					FormRow(
						onClick = dropUnlessResumed {
							backStack.add(Screen.Settings.Fonts)
						}
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.title_choose_font))
							Text(
								preferenceManager.font.displayName,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					var showThemeDialog by rememberSaveable { mutableStateOf(false) }
					FormRow(
						onClick = {
							showThemeDialog = true
						}
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_choose_theme))
							Text(
								stringResource(preferenceManager.theme.title),
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}
					}

					ThemeDialog(
						presented = showThemeDialog,
						onDismissRequest = { showThemeDialog = false }
					)

					if (preferenceManager.theme == Theme.Seeded) {
						var expanded by remember { mutableStateOf(false) }
						FormRow {
							Text(stringResource(Res.string.option_accent_colour))
							Box {
								Box(
									Modifier
										.clip(CircleShape)
										.background(
											HsvColor(
												preferenceManager.accentColourH,
												preferenceManager.accentColourS,
												preferenceManager.accentColourV
											).toColor()
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
										RingColorPicker(
											color = {
												HsvColor(
													preferenceManager.accentColourH,
													preferenceManager.accentColourS,
													preferenceManager.accentColourV
												)
											},
											onColorChange = {
												preferenceManager.apply {
													accentColourH = it.hue
													accentColourS = it.saturation
													accentColourV = it.value
												}
											}
										)
									}
								}
							}
						}
					}
				}

				FormTitle(stringResource(Res.string.title_layout))
				Form {
					FormRow(
						onClick = {
							showArtworkShapeDialog = true
						}
					) {
						Column(Modifier.weight(1f)) {
							Text(stringResource(Res.string.option_artwork_shape))
							Text(
								preferenceManager.coverArtShape.name,
								style = MaterialTheme.typography.bodyMedium,
								color = MaterialTheme.colorScheme.onSurfaceVariant
							)
						}

						val shape = preferenceManager.coverArtShape.decreasedShape
						Box(
							modifier = Modifier
								.size(48.dp)
								.clip(shape)
								.background(MaterialTheme.colorScheme.primaryContainer)
								.border(2.dp, MaterialTheme.colorScheme.primary, shape)
						)
					}

					var presented by remember { mutableStateOf(false) }
					val onClick = { presented = true }
					FormRow(
						onClick = if (platformContext.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact)
							onClick
						else null
					) {
						if (platformContext.sizeClass.widthSizeClass <= WindowWidthSizeClass.Compact) {

							Column(Modifier.weight(1f)) {
								Text(stringResource(Res.string.option_grid_items_per_row))
								Text(
									preferenceManager.gridSize.label,
									style = MaterialTheme.typography.bodyMedium,
									color = MaterialTheme.colorScheme.onSurfaceVariant
								)
							}

							GridSizePreview(preferenceManager.gridSize.value)

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
										"${preferenceManager.artGridItemSize}",
										fontFamily = FontFamily.Monospace,
										fontWeight = FontWeight(400),
										fontSize = 13.sp,
										color = MaterialTheme.colorScheme.onSurfaceVariant,
									)
								}
								Slider(
									value = preferenceManager.artGridItemSize,
									onValueChange = {
										preferenceManager.artGridItemSize = it
									},
									valueRange = 50f..500f,
									steps = 8,
								)
							}
						}
					}
				}

				FormTitle(stringResource(Res.string.title_miscellaneous))
				Form {
					SettingSelectionRow(
						title = { Text(stringResource(Res.string.option_use_marquee_text)) },
						items = MarqueeSpeed.entries.toImmutableList(),
						label = { it.name },
						selection = preferenceManager.marqueeSpeed,
						onSelect = { preferenceManager.marqueeSpeed = it }
					)

					SettingSwitchRow(
						title = { Text(stringResource(Res.string.option_alphabetical_scroll)) },
						value = preferenceManager.alphabeticalScroll,
						onSetValue = { preferenceManager.alphabeticalScroll = it }
					)

					SettingSelectionRow(
						title = { Text(stringResource(Res.string.option_animation_style)) },
						items = AnimationStyle.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = preferenceManager.animationStyle,
						onSelect = { preferenceManager.animationStyle = it }
					)
				}
			}
		}
		ArtworkShapeDialog(
			presented = showArtworkShapeDialog,
			onDismissRequest = { showArtworkShapeDialog = false }
		)
	}
}

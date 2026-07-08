package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed
import com.kyant.capsule.ContinuousRoundedRectangle
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import dev.zt64.compose.pipette.HsvColor
import dev.zt64.compose.pipette.RingColorPicker
import kotlinx.collections.immutable.toImmutableList
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.option_accent_colour
import navic.composeapp.generated.resources.option_choose_theme
import navic.composeapp.generated.resources.option_palette_specification
import navic.composeapp.generated.resources.option_palette_style
import navic.composeapp.generated.resources.title_palette
import navic.composeapp.generated.resources.title_theme_mode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.Theme
import paige.navic.domain.models.settings.ThemeMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Picker
import paige.navic.ui.components.common.Dropdown
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.common.TooltipBox
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.screens.settings.components.SettingSelectionRow
import paige.navic.util.core.label

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsThemesScreen() {
	val preferenceManager = koinInject<PreferenceManager>()

	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.option_choose_theme)) }) },
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
					SettingSelectionRow(
						title = { Text(stringResource(Res.string.title_theme_mode)) },
						items = ThemeMode.entries.toImmutableList(),
						label = { stringResource(it.title) },
						selection = preferenceManager.themeMode,
						onSelect = { preferenceManager.themeMode = it }
					)
				}

				FormTitle(stringResource(Res.string.title_palette))
				Form {
					FormRow {
						LazyRow(
							modifier = Modifier
								.fillMaxWidth()
								.selectableGroup(),
							horizontalArrangement = Arrangement.SpaceBetween
						) {
							items(Theme.entries) { theme ->
								ThemeCard(
									theme = theme,
									isSelected = preferenceManager.theme == theme,
									onSelect = { preferenceManager.theme = theme }
								)
							}
						}
					}

					AnimatedVisibility(
						modifier = Modifier.fillMaxWidth(),
						visible = preferenceManager.theme == Theme.Seeded
					) {
						ThemeAccentPicker()
					}

					AnimatedVisibility(
						modifier = Modifier.fillMaxWidth(),
						visible = preferenceManager.theme == Theme.Seeded
					) {
						SettingSelectionRow(
							title = { Text(stringResource(Res.string.option_palette_style)) },
							items = PaletteStyle.entries.toImmutableList(),
							label = { it.label() },
							selection = preferenceManager.paletteStyle,
							onSelect = { preferenceManager.paletteStyle = it }
						)
					}

					AnimatedVisibility(
						modifier = Modifier.fillMaxWidth(),
						visible = preferenceManager.theme == Theme.Seeded
					) {
						SettingSelectionRow(
							title = { Text(stringResource(Res.string.option_palette_specification)) },
							items = ColorSpec.SpecVersion.entries.toImmutableList(),
							label = { it.label() },
							selection = preferenceManager.paletteSpec,
							onSelect = { preferenceManager.paletteSpec = it }
						)
					}
				}
			}
		}
	}
}

@Composable
private fun BaseCard(
	modifier: Modifier,
	isSelected: Boolean,
	onSelect: () -> Unit,
	square: Boolean = false,
	content: @Composable ColumnScope.() -> Unit
) {
	val haptics = LocalHapticFeedback.current
	val interactionSource = remember { MutableInteractionSource() }
	val isPressed by interactionSource.collectIsPressedAsState()

	val radius by animateDpAsState(
		if (square || isPressed || isSelected) 16.dp else 36.dp
	)
	val borderColor by animateColorAsState(
		if (isSelected)
			MaterialTheme.colorScheme.primary
		else Color.Transparent
	)
	val shape = ContinuousRoundedRectangle(radius)

	Box(
		modifier = modifier
			.size(64.dp)
			.border(4.dp, borderColor, shape)
			.clip(shape)
			.selectable(
				selected = isSelected,
				interactionSource = interactionSource,
				indication = ripple(),
				enabled = true,
				role = Role.ValuePicker,
				onClick = dropUnlessResumed {
					haptics.performHapticFeedback(HapticFeedbackType.ToggleOn)
					onSelect()
				}
			),
		contentAlignment = Alignment.Center
	) {
		Column(
			modifier = Modifier
				.size(50.dp)
				.clip(ContinuousRoundedRectangle(radius - 7.dp))
		) {
			content()
		}
	}
}

@Composable
private fun ThemeCard(
	theme: Theme,
	isSelected: Boolean,
	onSelect: () -> Unit
) {
	val colorScheme = theme.colorScheme()
	val title = stringResource(theme.title)

	TooltipBox(title) {
		BaseCard(
			modifier = Modifier.semantics {
				contentDescription = title
			},
			isSelected = isSelected,
			onSelect = onSelect,
		) {
			if (theme != Theme.Seeded) {
				Box(
					modifier = Modifier
						.weight(1f)
						.fillMaxSize()
						.background(colorScheme.primary)
				)

				Row(modifier = Modifier.weight(1f)) {
					Box(
						modifier = Modifier
							.weight(1f)
							.fillMaxSize()
							.background(colorScheme.secondary)
					)
					Box(
						modifier = Modifier
							.weight(1f)
							.fillMaxSize()
							.background(colorScheme.tertiary)
					)
				}
			} else {
				Box(
					modifier = Modifier
						.weight(1f)
						.fillMaxSize()
						.background(MaterialTheme.colorScheme.primaryContainer),
					contentAlignment = Alignment.Center
				) {
					Icon(
						Icons.Outlined.Picker,
						contentDescription = null,
						tint = MaterialTheme.colorScheme.onPrimaryContainer
					)
				}
			}
		}
	}
}

@Composable
private fun ThemeAccentPicker() {
	val preferenceManager = koinInject<PreferenceManager>()
	var expanded by remember { mutableStateOf(false) }

	FormRow(
		onClick = { expanded = true }
	) {
		Text(stringResource(Res.string.option_accent_colour))
		Box {
			Box(
				Modifier
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.primary)
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
								hue = preferenceManager.paletteAccentH,
								saturation = 1f,
								value = 1f
							)
						},
						onColorChange = { color ->
							preferenceManager.paletteAccentH = color.hue
						}
					)
				}
			}
		}
	}
}

package paige.navic.ui.screens.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.app_name
import navic.composeapp.generated.resources.title_playback
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.AppIconManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.AppIconVariant
import paige.navic.icons.Icons
import paige.navic.icons.brand.Navic
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar

@Composable
fun SettingsAppIconScreen() {
	val appIconManager = koinInject<AppIconManager>()
	val preferenceManager = koinInject<PreferenceManager>()
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_playback)) }) }
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
				AppIconPreview(preferenceManager.appIconVariant)

				Form(Modifier.selectableGroup()) {
					AppIconVariant.entries.forEach { variant ->
						FormRow(
							onClick = { appIconManager.setVariant(variant) },
							modifier = Modifier.semantics {
								selected = preferenceManager.appIconVariant == variant
							},
							horizontalArrangement = Arrangement.spacedBy(14.dp),
						) {
							RadioButton(
								selected = preferenceManager.appIconVariant == variant,
								onClick = null
							)
							Text(variant.name, Modifier.weight(1f))
						}
					}
				}
			}
		}
	}
}

@Composable
fun AppIconPreview(variant: AppIconVariant) {
	val appIconManager = koinInject<AppIconManager>()
	val icon = remember(variant) { appIconManager.getIcon(variant) }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(bottom = 24.dp)
			.clip(MaterialTheme.shapes.extraLarge)
			.background(MaterialTheme.colorScheme.surfaceContainerLow)
			.padding(vertical = 48.dp),
		contentAlignment = Alignment.Center
	) {
		Column(
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			val iconModifier = Modifier
				.size(84.dp)
				.clip(MaterialTheme.shapes.large)

			when (icon) {
				is ImageBitmap -> Image(
					bitmap = icon,
					contentDescription = null,
					modifier = iconModifier
				)
				is Painter -> Image(
					painter = icon,
					contentDescription = null,
					modifier = iconModifier
				)
				else -> {
					val iconBackground =
						if (variant == AppIconVariant.Inverted) Color.White else Color.Black
					val iconTint = if (variant == AppIconVariant.Inverted) Color.Black else Color.White

					Box(
						modifier = iconModifier.background(iconBackground),
						contentAlignment = Alignment.Center
					) {
						Icon(
							imageVector = Icons.Brand.Navic,
							contentDescription = null,
							tint = iconTint,
							modifier = Modifier.size(56.dp)
						)
					}
				}
			}

			Text(
				text = stringResource(Res.string.app_name),
				style = MaterialTheme.typography.labelLarge.copy(
					fontWeight = FontWeight.Medium,
					fontSize = 14.sp,
					letterSpacing = 0.5.sp
				),
				color = MaterialTheme.colorScheme.onSurface
			)
		}
	}
}

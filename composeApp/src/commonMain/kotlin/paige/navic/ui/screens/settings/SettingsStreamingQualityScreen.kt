package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_in_use
import navic.composeapp.generated.resources.info_streaming_quality
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
	// i dont think there's a point in making a viewmodel for this screen considering
	// all we need is to check for isOnline/isCellular
	val connectivityManager = koinInject<ConnectivityManager>()
	val isOnline by connectivityManager.isOnline.collectAsStateWithLifecycle()
	val isCellular by connectivityManager.isCellular.collectAsStateWithLifecycle()

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
					.padding(top = 16.dp, end = 16.dp, start = 16.dp)
			) {
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

				Icon(
					Icons.Outlined.Info,
					contentDescription = null,
					tint = MaterialTheme.colorScheme.onSurfaceVariant,
					modifier = Modifier.padding(horizontal = 9.dp)
				)
				Spacer(Modifier.height(16.dp))
				Text(
					stringResource(Res.string.info_streaming_quality),
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					style = MaterialTheme.typography.bodyMedium,
					modifier = Modifier.padding(horizontal = 8.dp)
				)
			}
		}
	}
}

@Composable
private fun RadioButtons(
	value: StreamingQuality,
	onChangeValue: (StreamingQuality) -> Unit
) {
	val ctx = LocalCtx.current
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

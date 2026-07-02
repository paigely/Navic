package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.title_playback
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.AppIconManager
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.models.settings.AppIconVariant
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

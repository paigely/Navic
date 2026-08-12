package paige.navic.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_reset
import navic.composeapp.generated.resources.info_equaliser_mode_not_builtin
import navic.composeapp.generated.resources.info_equaliser_unsupported
import navic.composeapp.generated.resources.option_equaliser
import navic.composeapp.generated.resources.option_equaliser_mode
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import paige.navic.domain.manager.EqualiserManager
import paige.navic.domain.models.settings.EqualiserMode
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Refresh
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.VerticalSlider
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.components.layouts.TopBarButton
import paige.navic.ui.screens.settings.components.SettingSelectionRow

@Composable
fun SettingsEqualiserScreen() {
	val equaliserManager = koinInject<EqualiserManager>()
	val config by equaliserManager.config.collectAsStateWithLifecycle()
	val scope = rememberCoroutineScope()

	Scaffold(
		topBar = {
			NestedTopBar(
				title = { Text(stringResource(Res.string.option_equaliser)) },
				actions = {
					TopBarButton(
						onClick = {
							scope.launch {
								equaliserManager.setConfig(
									config.copy(bandLevels = emptyMap())
								)
							}
						},
						enabled = config.bandLevels.isNotEmpty() && config.mode == EqualiserMode.BuiltIn
					) {
						Icon(
							imageVector = Icons.Outlined.Refresh,
							contentDescription = stringResource(Res.string.action_reset)
						)
					}
				}
			)
		}
	) { innerPadding ->
		CompositionLocalProvider(
			LocalMinimumInteractiveComponentSize provides 0.dp
		) {
			Column(
				Modifier
					.fillMaxWidth()
					.padding(innerPadding)
					.verticalScroll(rememberScrollState())
					.padding(top = 16.dp, end = 16.dp, start = 16.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Form(modifier = Modifier.widthIn(max = 600.dp)) {
					SettingSelectionRow(
						title = { Text(stringResource(Res.string.option_equaliser_mode)) },
						items = EqualiserMode.entries.toImmutableList(),
						label = { stringResource(it.displayName) },
						selection = config.mode,
						onSelect = { mode ->
							scope.launch {
								equaliserManager.setConfig(config.copy(mode = mode))
							}
						}
					)
				}

				if (config.mode != EqualiserMode.BuiltIn) {
					Text(stringResource(Res.string.info_equaliser_mode_not_builtin))
					return@Column
				}
				if (config.bandCount == 0) {
					Text(stringResource(Res.string.info_equaliser_unsupported))
					return@Column
				}
				Row(Modifier.widthIn(max = 600.dp)) {
					repeat(config.bandCount) { band ->
						EqualiserBand(
							level = config.bandLevels[band] ?: 0f,
							onLevelChange = { level ->
								val newLevels = config.bandLevels.toMutableMap().apply {
									set(band, level)
								}
								val newConfig = config.copy(bandLevels = newLevels)
								scope.launch {
									equaliserManager.setConfig(newConfig)
								}
							},
							levelRange = config.bandLowerRange..config.bandUpperRange
						)
					}
				}
			}
		}
	}
}

@Composable
private fun RowScope.EqualiserBand(
	level: Float,
	onLevelChange: (level: Float) -> Unit,
	levelRange: ClosedFloatingPointRange<Float>
) {
	val textStyle = MaterialTheme.typography.bodySmall.copy(
		fontFamily = FontFamily.Monospace,
		fontWeight = FontWeight.Medium
	)
	Column(
		modifier = Modifier.height(400.dp).weight(1f),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Text("${levelRange.endInclusive.toInt()}mB", style = textStyle, maxLines = 1)
		VerticalSlider(
			modifier = Modifier.weight(1f),
			value = level,
			onValueChange = onLevelChange,
			valueRange = levelRange
		)
		Text("${levelRange.start.toInt()}mB", style = textStyle, maxLines = 1)
	}
}

package paige.navic.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.action_delete
import navic.composeapp.generated.resources.action_new
import navic.composeapp.generated.resources.option_custom_headers
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.session.SessionManager
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Add
import paige.navic.icons.outlined.Delete
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.common.FormTitle
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.theme.defaultFont
import kotlin.random.Random

private data class Header(
	val id: Long = Random.nextLong(),
	val key: String,
	val value: String
)

@Composable
fun SettingsCustomHeadersScreen() {
	val ctx = LocalCtx.current

	val headers = remember {
		Settings.shared.customHeaders.lines()
			.filter { it.contains(":") }
			.map {
				val parts = it.split(":", limit = 2)
				Header(key = parts[0], value = parts[1])
			}
			.toMutableStateList()
	}

	val hiddenHeaders = remember { mutableStateSetOf<Long>() }

	fun updateSettings() {
		Settings.shared.customHeaders = headers
			.filter { !hiddenHeaders.contains(it.id) }
			.joinToString("\n") { "${it.key}:${it.value}" }
		SessionManager.refreshClient()
	}

	Scaffold(
		topBar = {
			NestedTopBar(
				{ Text(stringResource(Res.string.option_custom_headers)) },
				hideBack = ctx.sizeClass.widthSizeClass >= WindowWidthSizeClass.Medium
			)
		}
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
				FormTitle(stringResource(Res.string.option_custom_headers))
				Form(
					Modifier.animateContentSize().fillMaxWidth(),
					bottomPadding = 8.dp
				) {
					headers.forEachIndexed { index, header ->
						AnimatedVisibility(
							modifier = Modifier.fillMaxWidth(),
							visible = !hiddenHeaders.contains(header.id)
						) {
							HeaderRow(
								key = header.key,
								value = header.value,
								onSetKey = {
									headers[index] = header.copy(key = it)
									updateSettings()
								},
								onSetValue = {
									headers[index] = header.copy(value = it)
									updateSettings()
								},
								onDelete = {
									hiddenHeaders.add(header.id)
									updateSettings()
								}
							)
						}
					}
				}
				FilledTonalButton(
					onClick = {
						ctx.clickSound()
						headers.add(Header(key = "", value = ""))
						updateSettings()
					},
					modifier = Modifier.fillMaxWidth()
				) {
					Icon(Icons.Outlined.Add, null)
					Spacer(Modifier.width(8.dp))
					Text(
						stringResource(Res.string.action_new),
						fontFamily = defaultFont(100)
					)
				}
			}
		}
	}
}

@Composable
private fun HeaderRow(
	key: String,
	value: String,
	onSetKey: (String) -> Unit,
	onSetValue: (String) -> Unit,
	onDelete: () -> Unit
) {
	FormRow(
		horizontalArrangement = Arrangement.spacedBy(8.dp)
	) {
		Column(
			Modifier.weight(1f),
			verticalArrangement = Arrangement.spacedBy(8.dp)
		) {
			TextField(
				value = key,
				onValueChange = onSetKey,
				placeholder = { Text("Key") },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				),
				shape = MaterialTheme.shapes.medium
			)
			TextField(
				value = value,
				onValueChange = onSetValue,
				placeholder = { Text("Value") },
				modifier = Modifier.fillMaxWidth(),
				singleLine = true,
				colors = TextFieldDefaults.colors(
					focusedIndicatorColor = Color.Transparent,
					unfocusedIndicatorColor = Color.Transparent
				),
				shape = MaterialTheme.shapes.medium
			)
		}
		FilledTonalButton(
			onClick = onDelete,
			contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
			shape = MaterialTheme.shapes.medium
		) {
			Icon(Icons.Outlined.Delete, stringResource(Res.string.action_delete))
		}
	}
}

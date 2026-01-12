package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import dev.burnoo.compose.remembersetting.rememberFloatSetting
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.keyboard_arrow_down
import navic.composeapp.generated.resources.keyboard_arrow_up
import navic.composeapp.generated.resources.option_cover_art_size
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_round_album_covers
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_system_font
import navic.composeapp.generated.resources.title_appearance
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.dialog.NavtabsDialog
import paige.navic.ui.theme.mapleMono

@Composable
fun ThemeSettings() {
	var expanded by rememberSaveable { mutableStateOf(false) }
	var showNavtabsDialog by rememberSaveable { mutableStateOf(false) }
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
	var artGridSize by rememberFloatSetting("artGridSize", 150f)
	Form {
		FormRow(
			onClick = { expanded = !expanded }
		) {
			Text(stringResource(Res.string.title_appearance))
			Icon(
				if (expanded)
					vectorResource(Res.drawable.keyboard_arrow_up)
				else vectorResource(Res.drawable.keyboard_arrow_down),
				contentDescription = null
			)
		}
		if (expanded) {
			FormRow {
				Text(stringResource(Res.string.option_system_font))
				Switch(
					checked = useSystemFont,
					onCheckedChange = { useSystemFont = it }
				)
			}
			FormRow {
				Text(stringResource(Res.string.option_short_navigation_bar))
				Switch(
					checked = useShortNavbar,
					onCheckedChange = { useShortNavbar = it }
				)
			}
			FormRow {
				Text(stringResource(Res.string.option_round_album_covers))
				Switch(
					checked = roundCoverArt,
					onCheckedChange = { roundCoverArt = it }
				)
			}
			FormRow {
				Column(Modifier.fillMaxWidth()) {
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.SpaceBetween
					) {
						Text(stringResource(Res.string.option_cover_art_size))
						Text(
							"$artGridSize",
							fontFamily = mapleMono(),
							fontWeight = FontWeight(400),
							fontSize = 13.sp,
							color = MaterialTheme.colorScheme.onSurface.copy(alpha = .5f),
						)
					}
					Slider(
						value = artGridSize,
						onValueChange = {
							artGridSize = it
						},
						valueRange = 50f..500f,
						steps = 8,
					)
				}
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
	NavtabsDialog(
		presented = showNavtabsDialog,
		onDismissRequest = { showNavtabsDialog = false }
	)
}

@Composable
fun SettingsScreen() {
	val scrollState = rememberScrollState()
	Form(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.surface)
			.verticalScroll(scrollState)
			.padding(12.dp)
	) {
		ThemeSettings()
	}
}

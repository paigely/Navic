package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.burnoo.compose.remembersetting.rememberBooleanSetting
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.keyboard_arrow_down
import navic.composeapp.generated.resources.keyboard_arrow_up
import navic.composeapp.generated.resources.option_navbar_tab_positions
import navic.composeapp.generated.resources.option_round_album_covers
import navic.composeapp.generated.resources.option_short_navigation_bar
import navic.composeapp.generated.resources.option_system_font
import navic.composeapp.generated.resources.title_appearance
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.ui.component.Form
import paige.navic.ui.component.FormRow
import paige.navic.ui.component.dialog.NavbarTabsDialog

@Composable
fun ThemeSettings() {
	var expanded by rememberSaveable { mutableStateOf(false) }
	var showNavbarTabsDialog by rememberSaveable { mutableStateOf(false) }
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
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
			FormRow(
				onClick = {
					showNavbarTabsDialog = true
				}
			) {
				Text(stringResource(Res.string.option_navbar_tab_positions))
			}
		}
	}
	NavbarTabsDialog(
		presented = showNavbarTabsDialog,
		onDismissRequest = { showNavbarTabsDialog = false }
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

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
import org.jetbrains.compose.resources.vectorResource
import paige.navic.ui.component.Form
import paige.navic.ui.component.FormRow

@Composable
fun ThemeSettings() {
	var expanded by rememberSaveable { mutableStateOf(false) }
	var useSystemFont by rememberBooleanSetting("useSystemFont", false)
	var useShortNavbar by rememberBooleanSetting("useShortNavbar", false)
	var roundCoverArt by rememberBooleanSetting("roundCoverArt", true)
	var liquidGlass by rememberBooleanSetting("liquidGlass", false)
	Form {
		FormRow(
			onClick = { expanded = !expanded }
		) {
			Text("Appearance")
			Icon(
				if (expanded)
					vectorResource(Res.drawable.keyboard_arrow_up)
				else vectorResource(Res.drawable.keyboard_arrow_down),
				contentDescription = null
			)
		}
		if (expanded) {
			FormRow {
				Text("Use system font")
				Switch(
					checked = useSystemFont,
					onCheckedChange = { useSystemFont = it }
				)
			}
			FormRow {
				Text("Use short navigation bar")
				Switch(
					checked = useShortNavbar,
					onCheckedChange = { useShortNavbar = it }
				)
			}
			FormRow {
				Text("Rounded cover artwork")
				Switch(
					checked = roundCoverArt,
					onCheckedChange = { roundCoverArt = it }
				)
			}
			FormRow {
				Text("Liquid glass")
				Switch(
					checked = liquidGlass,
					onCheckedChange = { liquidGlass = it }
				)
			}
		}
	}
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

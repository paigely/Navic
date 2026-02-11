package paige.navic.ui.components.settings

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import paige.navic.icons.Icons
import paige.navic.icons.outlined.SwitchOff
import paige.navic.icons.outlined.SwitchOn

@Composable
fun SettingSwitch(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Switch(
		checked = checked,
		onCheckedChange = onCheckedChange,
		thumbContent = {
			Icon(
				if (checked) Icons.Outlined.SwitchOn else Icons.Outlined.SwitchOff,
				contentDescription = null,
				modifier = Modifier.size(SwitchDefaults.IconSize)
			)
		}
	)
}

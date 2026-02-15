package paige.navic.ui.components.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import paige.navic.data.models.Settings
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
		colors = SwitchDefaults.colors(
			uncheckedBorderColor = Color.Transparent,
			uncheckedThumbColor = Color.White
		),
		thumbContent = {
			if (Settings.shared.theme.isMaterialLike()) {
				Icon(
					if (checked) Icons.Outlined.SwitchOn else Icons.Outlined.SwitchOff,
					contentDescription = null,
					modifier = Modifier.size(SwitchDefaults.IconSize)
				)
			} else {
				Box(Modifier.size(24.dp).shadow(3.dp, CircleShape).background(Color.White, CircleShape))
			}
		}
	)
}

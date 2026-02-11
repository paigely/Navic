package paige.navic.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import paige.navic.ui.components.common.FormRow

@Composable
fun SettingSwitchRow(
	title: @Composable () -> Unit,
	subtitle: @Composable () -> Unit = {},
	value: Boolean,
	onSetValue: (Boolean) -> Unit
) {
	FormRow(onClick = { onSetValue(!value) }) {
		Column(Modifier.weight(1f)) {
			title()
			CompositionLocalProvider(
				LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			) {
				subtitle()
			}
		}
		VerticalDivider(Modifier.height(32.dp).padding(horizontal = 14.dp))
		SettingSwitch(
			checked = value,
			onCheckedChange = { onSetValue(it) }
		)
	}
}
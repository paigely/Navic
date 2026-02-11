package paige.navic.ui.components.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import com.kyant.capsule.ContinuousRoundedRectangle
import paige.navic.icons.Icons
import paige.navic.icons.outlined.ChevronForward
import paige.navic.ui.components.common.FormRow

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingCollapsibleRow(
	title: @Composable () -> Unit,
	subtitle: @Composable () -> Unit = {},
	value: Boolean,
	onSetValue: (Boolean) -> Unit,
	expandedContent: @Composable () -> Unit
) {
	var expanded by remember { mutableStateOf(false) }
	FormRow(onClick = { expanded = !expanded }) {
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
		Icon(
			Icons.Outlined.ChevronForward,
			null,
			modifier = Modifier.size(20.dp).rotate(
				if (expanded) 90f else 0f
			)
		)
		VerticalDivider(Modifier.height(32.dp).padding(horizontal = 14.dp))
		SettingSwitch(
			checked = value,
			onCheckedChange = { onSetValue(it) }
		)
	}
	AnimatedVisibility(expanded) {
		FormRow(
			modifier = Modifier.clip(ContinuousRoundedRectangle(
				topStart = 3.dp,
				topEnd = 3.dp,
				bottomStart = 18.dp,
				bottomEnd = 18.dp
			)),
			color = MaterialTheme.colorScheme.surfaceContainerLow
		) {
			Column(Modifier.weight(1f)) {
				expandedContent()
			}
		}
	}
}
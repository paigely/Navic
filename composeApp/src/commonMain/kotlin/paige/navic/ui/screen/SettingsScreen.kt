package paige.navic.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.palette
import navic.composeapp.generated.resources.settings
import navic.composeapp.generated.resources.subtitle_appearance
import navic.composeapp.generated.resources.subtitle_behaviour
import navic.composeapp.generated.resources.title_appearance
import navic.composeapp.generated.resources.title_behaviour
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalNavStack
import paige.navic.SettingsAppearance
import paige.navic.SettingsBehaviour
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow

@Composable
fun SettingsScreen() {
	Form(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.surface)
			.verticalScroll(rememberScrollState())
			.padding(12.dp)
			.padding(bottom = 117.9.dp)
	) {
		PageRow(
			destination = SettingsAppearance,
			icon = Res.drawable.palette,
			title = Res.string.title_appearance,
			subtitle = Res.string.subtitle_appearance,
			foregroundColor = Color(0xFF753403),
			backgroundColor = Color(0xFFFFB683)
		)
		PageRow(
			destination = SettingsBehaviour,
			icon = Res.drawable.settings,
			title = Res.string.title_behaviour,
			subtitle = Res.string.subtitle_behaviour,
			foregroundColor = Color(0xFF004D68),
			backgroundColor = Color(0xFF67D4FF)
		)
	}
}

@Composable
private fun PageRow(
	destination: Any,
	icon: DrawableResource,
	title: StringResource,
	subtitle: StringResource,
	foregroundColor: Color,
	backgroundColor: Color
) {
	val backStack = LocalNavStack.current
	FormRow(
		onClick = {
			backStack.add(destination)
		},
		horizontalArrangement = Arrangement.spacedBy(16.dp),
		contentPadding = PaddingValues(horizontal = 18.dp, vertical = 18.dp)
	) {
		Icon(
			vectorResource(icon),
			contentDescription = null,
			modifier = Modifier
				.size(42.dp)
				.background(backgroundColor, CircleShape)
				.padding(10.dp),
			tint = foregroundColor
		)
		Column {
			Text(
				stringResource(title),
				style = MaterialTheme.typography.titleMedium
			)
			Text(
				stringResource(subtitle),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

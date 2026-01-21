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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info
import navic.composeapp.generated.resources.palette
import navic.composeapp.generated.resources.settings
import navic.composeapp.generated.resources.subtitle_about
import navic.composeapp.generated.resources.subtitle_appearance
import navic.composeapp.generated.resources.subtitle_behaviour
import navic.composeapp.generated.resources.title_about
import navic.composeapp.generated.resources.title_appearance
import navic.composeapp.generated.resources.title_behaviour
import navic.composeapp.generated.resources.title_settings
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import paige.navic.LocalNavStack
import paige.navic.data.model.Screen
import paige.navic.ui.component.common.Form
import paige.navic.ui.component.common.FormRow
import paige.navic.ui.component.layout.NestedTopBar

@Composable
fun SettingsScreen() {
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_settings)) }) }
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(12.dp)
				.padding(bottom = 117.9.dp)
		) {
			Form {
				PageRow(
					destination = Screen.Settings.Appearance,
					icon = Res.drawable.palette,
					title = Res.string.title_appearance,
					subtitle = Res.string.subtitle_appearance,
					foregroundColor = Color(0xFF753403),
					backgroundColor = Color(0xFFFFB683)
				)
				PageRow(
					destination = Screen.Settings.Behaviour,
					icon = Res.drawable.settings,
					title = Res.string.title_behaviour,
					subtitle = Res.string.subtitle_behaviour,
					foregroundColor = Color(0xFF004D68),
					backgroundColor = Color(0xFF67D4FF)
				)
			}
			Form {
				PageRow(
					destination = Screen.Settings.About,
					icon = Res.drawable.info,
					title = Res.string.title_about,
					subtitle = Res.string.subtitle_about,
					foregroundColor = Color(0xFF2C2C2C),
					backgroundColor = Color(0xFFC7C7C7)
				)
			}
		}
	}
}

@Composable
private fun PageRow(
	destination: NavKey,
	icon: DrawableResource,
	title: StringResource,
	subtitle: StringResource,
	foregroundColor: Color,
	backgroundColor: Color
) {
	val backStack = LocalNavStack.current
	FormRow(
		onClick = {
			backStack.lastOrNull()?.let {
				if (it is Screen.Settings) {
					if (it !is Screen.Settings.Root) {
						backStack.removeLastOrNull()
					}
					backStack.add(destination)
				}
			}
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

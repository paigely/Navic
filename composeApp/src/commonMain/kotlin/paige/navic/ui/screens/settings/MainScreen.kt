package paige.navic.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.subtitle_about
import navic.composeapp.generated.resources.subtitle_appearance
import navic.composeapp.generated.resources.subtitle_bottom_app_bar
import navic.composeapp.generated.resources.subtitle_chat
import navic.composeapp.generated.resources.subtitle_now_playing
import navic.composeapp.generated.resources.subtitle_scrobbling
import navic.composeapp.generated.resources.subtitle_source
import navic.composeapp.generated.resources.title_about
import navic.composeapp.generated.resources.title_appearance
import navic.composeapp.generated.resources.title_bottom_app_bar
import navic.composeapp.generated.resources.title_chat
import navic.composeapp.generated.resources.title_now_playing
import navic.composeapp.generated.resources.title_scrobbling
import navic.composeapp.generated.resources.title_settings
import navic.composeapp.generated.resources.title_source
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalContentPadding
import paige.navic.LocalNavStack
import paige.navic.data.models.Screen
import paige.navic.icons.Icons
import paige.navic.icons.brand.Discord
import paige.navic.icons.brand.Github
import paige.navic.icons.filled.Airwave
import paige.navic.icons.filled.BottomNavigation
import paige.navic.icons.filled.Palette
import paige.navic.icons.filled.Play
import paige.navic.icons.outlined.Info
import paige.navic.ui.components.common.Form
import paige.navic.ui.components.common.FormRow
import paige.navic.ui.components.layouts.NestedTopBar
import paige.navic.ui.theme.defaultFont

@Composable
fun SettingsScreen() {
	Scaffold(
		topBar = { NestedTopBar({ Text(stringResource(Res.string.title_settings)) }) },
		contentWindowInsets = WindowInsets.statusBars
	) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.verticalScroll(rememberScrollState())
				.padding(top = 16.dp, end = 16.dp, start = 16.dp)
		) {
			Form {
				PageRow(
					destination = Screen.Settings.Appearance,
					icon = Icons.Filled.Palette,
					iconSize = 24.dp,
					title = Res.string.title_appearance,
					subtitle = Res.string.subtitle_appearance,
					foregroundColor = Color(0xFF753403),
					backgroundColor = Color(0xFFFFB683)
				)
				PageRow(
					destination = Screen.Settings.BottomAppBar,
					icon = Icons.Filled.BottomNavigation,
					iconSize = 24.dp,
					title = Res.string.title_bottom_app_bar,
					subtitle = Res.string.subtitle_bottom_app_bar,
					foregroundColor = Color(0xFF8C0052),
					backgroundColor = Color(0xFFFDACE2)
				)
				PageRow(
					destination = Screen.Settings.NowPlaying,
					icon = Icons.Filled.Play,
					iconSize = 24.dp,
					title = Res.string.title_now_playing,
					subtitle = Res.string.subtitle_now_playing,
					foregroundColor = Color(0xFF8C0052),
					backgroundColor = Color(0xFFFDACE2)
				)
				PageRow(
					destination = Screen.Settings.Scrobbling,
					icon = Icons.Filled.Airwave,
					iconSize = 24.dp,
					title = Res.string.title_scrobbling,
					subtitle = Res.string.subtitle_scrobbling,
					foregroundColor = Color(0xFF043F9E),
					backgroundColor = Color(0xFFA0C7FD)
				)
			}
			Form {
				PageRow(
					destination = Screen.Settings.About,
					icon = Icons.Outlined.Info,
					title = Res.string.title_about,
					subtitle = Res.string.subtitle_about,
					foregroundColor = Color(0xFF2C2C2C),
					backgroundColor = Color(0xFFC7C7C7)
				)
				PageRow(
					link = "https://github.com/paigely/Navic",
					icon = Icons.Brand.Github,
					title = Res.string.title_source,
					subtitle = Res.string.subtitle_source,
					foregroundColor = Color(0xFF2C2C2C),
					backgroundColor = Color(0xFFC7C7C7)
				)
				PageRow(
					link = "https://discord.gg/TBcnNX66PH",
					icon = Icons.Brand.Discord,
					title = Res.string.title_chat,
					subtitle = Res.string.subtitle_chat,
					foregroundColor = Color(0xFF2C2C2C),
					backgroundColor = Color(0xFFC7C7C7)
				)
			}
			Spacer(Modifier.height(LocalContentPadding.current.calculateBottomPadding()))
		}
	}
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PageRow(
	link: String? = null,
	destination: NavKey? = null,
	icon: ImageVector,
	iconSize: Dp = 22.dp,
	title: StringResource,
	subtitle: StringResource,
	foregroundColor: Color,
	backgroundColor: Color
) {
	val uriHandler = LocalUriHandler.current
	val backStack = LocalNavStack.current
	FormRow(
		onClick = {
			link?.let { link ->
				uriHandler.openUri(link)
			}
			destination?.let { destination ->
				backStack.lastOrNull()?.let {
					if (it is Screen.Settings) {
						if (it !is Screen.Settings.Root) {
							backStack.removeLastOrNull()
						}
						backStack.add(destination)
					}
				}
			}
		},
		horizontalArrangement = Arrangement.spacedBy(12.dp),
		contentPadding = PaddingValues(16.dp)
	) {
		Column(
			modifier = Modifier
				.size(40.dp)
				.background(backgroundColor, CircleShape),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Icon(
				icon,
				contentDescription = null,
				modifier = Modifier.size(iconSize),
				tint = foregroundColor
			)
		}
		Column {
			Text(
				stringResource(title),
				style = MaterialTheme.typography.titleSmall.copy(
					fontFamily = defaultFont(100)
				)
			)
			Text(
				stringResource(subtitle),
				style = MaterialTheme.typography.bodyMedium.copy(
					fontFamily = defaultFont(width = 90f, round = 100f),
					lineHeight = 15.sp
				),
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}

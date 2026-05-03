package paige.navic.ui.screens.radio.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.materialkolor.rememberDynamicColorScheme
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.info_unknown
import org.jetbrains.compose.resources.stringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings
import paige.navic.data.models.settings.enums.ThemeMode
import paige.navic.domain.models.DomainRadio
import paige.navic.icons.Icons
import paige.navic.icons.outlined.Radio
import paige.navic.ui.theme.defaultFont
import kotlin.math.abs

@Composable
fun RadioListScreenCard(
	modifier: Modifier = Modifier,
	radio: DomainRadio,
	onPlayClick: () -> Unit
) {
	val ctx = LocalCtx.current
	val inDarkTheme = isSystemInDarkTheme()

	val isDark = remember(Settings.shared.themeMode) {
		when (Settings.shared.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}

	val seedColor = remember(radio.name) {
		HsvColor(
			hue = abs(radio.name.hashCode() % 360).toFloat(),
			saturation = 0.5f,
			value = 0.6f
		).toColor()
	}

	val colorScheme = rememberDynamicColorScheme(
		seedColor = seedColor,
		isDark = isDark
	)

	Surface(
		modifier = modifier,
		color = colorScheme.primaryContainer,
		contentColor = colorScheme.onPrimaryContainer,
		shape = MaterialTheme.shapes.medium,
		shadowElevation = 2.dp,
		onClick = {
			ctx.clickSound()
			onPlayClick()
		}
	) {
		Box(modifier = Modifier.height(100.dp).fillMaxWidth()) {

			Icon(
				imageVector = Icons.Outlined.Radio,
				contentDescription = null,
				modifier = Modifier
					.align(Alignment.CenterEnd)
					.size(120.dp)
					.offset(x = 20.dp, y = 20.dp)
					.rotate(-15f)
					.alpha(0.2f),
				tint = colorScheme.onPrimaryContainer
			)

			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				verticalArrangement = Arrangement.SpaceBetween
			) {
				Text(
					text = radio.name,
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold,
					fontFamily = defaultFont(round = 100f),
					overflow = TextOverflow.Ellipsis,
					maxLines = 2
				)

				Text(
					text = radio.homepageUrl?.removePrefix("http://")?.removePrefix("https://")?.trimEnd('/')
						?: stringResource(Res.string.info_unknown),
					style = MaterialTheme.typography.labelMedium,
					maxLines = 1,
					overflow = TextOverflow.Ellipsis,
					modifier = Modifier.alpha(0.8f)
				)
			}
		}
	}
}

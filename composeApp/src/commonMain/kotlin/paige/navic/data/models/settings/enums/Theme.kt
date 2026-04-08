package paige.navic.data.models.settings.enums

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import dev.zt64.compose.pipette.HsvColor
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.theme_apple_music
import navic.composeapp.generated.resources.theme_dynamic
import navic.composeapp.generated.resources.theme_ios
import navic.composeapp.generated.resources.theme_seeded
import navic.composeapp.generated.resources.theme_spotify
import navic.composeapp.generated.resources.theme_subtitle_apple_music
import navic.composeapp.generated.resources.theme_subtitle_dynamic
import navic.composeapp.generated.resources.theme_subtitle_ios
import navic.composeapp.generated.resources.theme_subtitle_seeded
import navic.composeapp.generated.resources.theme_subtitle_spotify
import org.jetbrains.compose.resources.StringResource
import paige.navic.LocalCtx
import paige.navic.data.models.settings.Settings.Companion.shared
import paige.navic.utils.darkIosColorScheme
import paige.navic.utils.lightIosColorScheme

/**
 * Theme choices that the user can choose from
 */
enum class Theme(
	val title: StringResource, val subtitle: StringResource
) {

	/**
	 * The app will be themed based on whatever the user
	 * chose in system settings. Android only.
	 */
	Dynamic(Res.string.theme_dynamic, Res.string.theme_subtitle_dynamic),

	/**
	 * The app will be themed based on a "seed" colour.
	 *
	 * When this is selected, `accentColor(H/S/V)` settings
	 * will be exposed in the UI as a colour picker.
	 */
	Seeded(Res.string.theme_seeded, Res.string.theme_subtitle_seeded),

	/**
	 * The app will be themed according to Apple's HIG.
	 * TODO: this should pull from UIColor
	 */
	@Suppress("EnumEntryName")
	iOS(Res.string.theme_ios, Res.string.theme_subtitle_ios),

	/**
	 * The same as iOS, but with a pink-ish accent.
	 */
	AppleMusic(Res.string.theme_apple_music, Res.string.theme_subtitle_apple_music),

	/**
	 * The same as iOS, but with a green accent.
	 */
	Spotify(Res.string.theme_spotify, Res.string.theme_subtitle_spotify);

	@OptIn(ExperimentalMaterial3ExpressiveApi::class)
	@Composable
	fun colorScheme(): ColorScheme {
		val ctx = LocalCtx.current
		val inDarkTheme = isSystemInDarkTheme()
		val isDark = remember(shared.themeMode) {
			when (shared.themeMode) {
				ThemeMode.System -> inDarkTheme
				ThemeMode.Dark -> true
				ThemeMode.Light -> false
			}
		}
		return when (this) {
			Dynamic -> ctx.colorScheme ?: remember(isDark) {
				if (isDark)
					darkColorScheme()
				else expressiveLightColorScheme()
			}

			Seeded -> rememberDynamicColorScheme(
				seedColor = HsvColor(
					shared.accentColourH,
					shared.accentColourS,
					shared.accentColourV
				).toColor(),
				isDark = isDark,
				specVersion = ColorSpec.SpecVersion.SPEC_2025,
			)

			iOS -> if (isDark)
				darkIosColorScheme(Color(0, 145, 255))
			else lightIosColorScheme(Color(0, 136, 255))

			AppleMusic -> if (isDark)
				darkIosColorScheme(Color(255, 55, 95))
			else lightIosColorScheme(Color(255, 45, 85))

			Spotify -> if (isDark)
				darkIosColorScheme(Color(30, 215, 96))
			else lightIosColorScheme(Color(30, 215, 96))
		}
	}

	fun isMaterialLike(): Boolean = when (this) {
		Dynamic,
		Seeded -> true

		else -> false
	}
}

package paige.navic.util.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import coil3.SingletonImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.kmpalette.rememberDominantColorState
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.koin.compose.koinInject
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.settings.ThemeMode
import paige.navic.shared.MediaPlayerViewModel
import coil3.compose.LocalPlatformContext as LocalCoilPlatformContext

@Composable
fun rememberColorSchemeFromCoverArt(
	coverArtId: String?,
	forceDark: Boolean = false,
	style: PaletteStyle = PaletteStyle.Content,
	specVersion: ColorSpec.SpecVersion = ColorSpec.SpecVersion.SPEC_2021
): ColorScheme {
	val sessionManager = koinInject<SessionManager>()
	val coverArtUri = remember(coverArtId) {
		coverArtId?.let { sessionManager.getCoverArtUrl(it) }
	}

	val coilPlatformContext = LocalCoilPlatformContext.current
	val loader = SingletonImageLoader.get(coilPlatformContext)
	val model = remember(coverArtUri) {
		ImageRequest.Builder(coilPlatformContext)
			.data(coverArtUri)
			.memoryCacheKey(coverArtId)
			.diskCacheKey(coverArtId)
			.diskCachePolicy(CachePolicy.ENABLED)
			.memoryCachePolicy(CachePolicy.ENABLED)
			.build()
	}
	val dominantColorState = rememberDominantColorState(cacheSize = 4)

	LaunchedEffect(model) {
		val result = loader.execute(model)
		result.image?.toImageBitmap()?.let { imageBitmap ->
			dominantColorState.updateFrom(imageBitmap)
		}
	}

	val preferenceManager = koinInject<PreferenceManager>()
	val inDarkTheme = isSystemInDarkTheme()
	val isDark = remember(forceDark, preferenceManager.themeMode) {
		forceDark || when (preferenceManager.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}
	val scheme = rememberDynamicColorScheme(
		seedColor = dominantColorState.color,
		isDark = isDark,
		style = style,
		specVersion = specVersion
	)

	return scheme
}

@Composable
fun rememberColorSchemeForCurrentSong(): ColorScheme {
	val player = koinInject<MediaPlayerViewModel>()
	val playerState by player.uiState.collectAsState()
	val coverArtId = playerState.currentSong?.coverArtId
	return rememberColorSchemeFromCoverArt(
		coverArtId = coverArtId,
		forceDark = true,
		style = if (coverArtId != null) PaletteStyle.Content else PaletteStyle.Monochrome
	)
}

private val IosRed = Color(255, 66, 69)

@Composable
fun lightIosColorScheme(
	accent: Color
): ColorScheme {
	return rememberDynamicColorScheme(
		primary = Color.White,
		isDark = false,
		isAmoled = true,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		style = PaletteStyle.Content,
		modifyColorScheme = { scheme ->
			scheme.copy(
				primary = accent,
				onPrimary = Color.White,
				primaryContainer = accent.copy(alpha = .3f),
				onPrimaryContainer = accent,
				secondaryContainer = accent.copy(alpha = .3f),
				onSecondaryContainer = accent,
				secondary = accent,
				tertiaryContainer = accent.copy(alpha = .3f),
				onTertiaryContainer = accent,
				tertiary = accent,
				error = IosRed,
				onError = Color.White,
				errorContainer = IosRed,
				onErrorContainer = Color.White,
				surfaceVariant = Color(224, 221, 220)
			)
		}
	)
}

@Composable
fun darkIosColorScheme(
	accent: Color
): ColorScheme {
	return rememberDynamicColorScheme(
		primary = Color.White,
		isDark = true,
		isAmoled = true,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		style = PaletteStyle.Content,
		modifyColorScheme = { scheme ->
			scheme.copy(
				primary = accent,
				onPrimary = Color.White,
				primaryContainer = accent.copy(alpha = .3f),
				onPrimaryContainer = accent,
				secondaryContainer = accent.copy(alpha = .3f),
				onSecondaryContainer = accent,
				secondary = accent,
				tertiaryContainer = accent.copy(alpha = .3f),
				onTertiaryContainer = accent,
				tertiary = accent,
				error = IosRed,
				onError = Color.White,
				errorContainer = IosRed,
				onErrorContainer = Color.White,
				surfaceVariant = Color(44, 44, 46),
				onSurfaceVariant = Color(142, 142, 147)
			)
		}
	)
}

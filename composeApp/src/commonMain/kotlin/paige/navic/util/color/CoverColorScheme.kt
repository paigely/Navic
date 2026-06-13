package paige.navic.util.color

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.kmpalette.color
import com.kmpalette.palette.graphics.Palette
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import org.koin.compose.koinInject
import paige.navic.di.getStaticImageLoader
import paige.navic.domain.manager.PreferenceManager
import paige.navic.domain.manager.SessionManager
import paige.navic.domain.models.settings.ThemeMode
import paige.navic.domain.manager.CoverColorManager

@Composable
fun rememberCoverColorScheme(coverArtId: String?): ColorScheme {
	val sessionManager = koinInject<SessionManager>()
	val colorManager = koinInject<CoverColorManager>()
	val preferenceManager = koinInject<PreferenceManager>()
	val inDarkTheme = isSystemInDarkTheme()

	val isDark = remember(preferenceManager.themeMode, inDarkTheme) {
		when (preferenceManager.themeMode) {
			ThemeMode.System -> inDarkTheme
			ThemeMode.Dark -> true
			ThemeMode.Light -> false
		}
	}

	val coverUri = remember(coverArtId) {
		coverArtId?.let { sessionManager.getCoverArtUrl(it) }
	}

	val coilContext = LocalPlatformContext.current
	val imageLoader = remember(coilContext) { getStaticImageLoader(coilContext) }

	var dominantColor by remember(coverArtId) {
		mutableStateOf(coverArtId?.let { colorManager.getColor(it) } ?: Color.Transparent)
	}

	val scheme = rememberDynamicColorScheme(
		seedColor = dominantColor,
		isDark = isDark,
		style = if (coverUri != null) PaletteStyle.Content else PaletteStyle.Monochrome,
		specVersion = ColorSpec.SpecVersion.SPEC_2021,
		modifyColorScheme = { baseScheme ->
			if (dominantColor != Color.Transparent) {
				baseScheme.copy(
					surface = baseScheme.surfaceContainer,
					background = baseScheme.surfaceContainer,
				)
			} else {
				baseScheme
			}
		}
	)

	LaunchedEffect(coverUri, coverArtId) {
		if (coverUri != null && coverArtId != null && dominantColor == Color.Transparent) {
			val request = ImageRequest.Builder(coilContext)
				.data(coverUri)
				.diskCacheKey(coverArtId)
				.size(128)
				.build()

			val result = imageLoader.execute(request)
			if (result is SuccessResult) {
				val bitmap = result.image.toComposeImageBitmap(coilContext)
				val palette = Palette.from(bitmap).generate()
				val color = palette.dominantSwatch?.color ?: Color.Transparent
				dominantColor = color
				colorManager.putColor(coverArtId, color)
			}
		} else if (coverArtId == null) {
			dominantColor = Color.Transparent
		}
	}

	return scheme
}

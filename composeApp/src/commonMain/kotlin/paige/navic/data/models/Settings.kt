// Mostly taken from https://github.com/zt64/tau/blob/main/core/src/main/kotlin/dev/zt64/tau/domain/manager/PreferencesManager.kt
// Copyright (c) 2025 zt64
// SPDX-License-Identifier: GPL-3.0

package paige.navic.data.models

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicColorScheme
import com.russhwolf.settings.get
import com.russhwolf.settings.set
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
import paige.navic.utils.darkIosColorScheme
import paige.navic.utils.lightIosColorScheme
import paige.subsonic.api.models.ListType
import kotlin.enums.enumEntries
import kotlin.reflect.KProperty

private typealias Getter<T> = (key: String, defaultValue: T) -> T
private typealias Setter<T> = (key: String, newValue: T) -> Unit

/**
 * Base class for managing preferences.
 *
 * @property settings
 */
@Suppress("SameParameterValue", "MemberVisibilityCanBePrivate")
abstract class BasePreferenceManager(
	protected val settings: com.russhwolf.settings.Settings
) {
	protected fun preference(
		key: String?,
		defaultValue: String
	) = PreferenceProvider(
		key = key,
		defaultValue = defaultValue,
		getter = settings::get,
		setter = settings::set
	)

	protected fun preference(defaultValue: String) = preference(null, defaultValue)

	protected fun preference(
		key: String?,
		defaultValue: Boolean
	) = PreferenceProvider(
		key = key,
		defaultValue = defaultValue,
		getter = settings::get,
		setter = settings::set
	)

	protected fun preference(defaultValue: Boolean) = preference(null, defaultValue)

	protected fun preference(
		key: String?,
		defaultValue: Int
	) = PreferenceProvider(
		key = key,
		defaultValue = defaultValue,
		getter = settings::get,
		setter = settings::set
	)

	protected fun preference(defaultValue: Int) = preference(null, defaultValue)

	protected fun preference(
		key: String?,
		defaultValue: Float
	) = PreferenceProvider(
		key = key,
		defaultValue = defaultValue,
		getter = settings::get,
		setter = settings::set
	)

	protected fun preference(defaultValue: Float) = preference(null, defaultValue)

	protected fun preference(
		key: String?,
		defaultValue: Long
	) = PreferenceProvider(
		key = key,
		defaultValue = defaultValue,
		getter = settings::get,
		setter = settings::set
	)

	protected fun preference(defaultValue: Long) = preference(null, defaultValue)

	protected inline fun <reified E : Enum<E>> preference(
		key: String?,
		defaultValue: E
	) = PreferenceProvider(
		key = key,
		defaultValue = defaultValue,
		getter = settings::getEnum,
		setter = settings::putEnum
	)

	protected inline fun <reified E : Enum<E>> preference(defaultValue: E): PreferenceProvider<E> {
		return preference(null, defaultValue)
	}

	protected class Preferences<T>(
		private val key: String,
		defaultValue: T,
		getter: Getter<T>,
		private val setter: Setter<T>
	) {
		private var value by mutableStateOf(getter(key, defaultValue))

		operator fun getValue(
			thisRef: Any,
			property: KProperty<*>
		) = value

		operator fun setValue(
			thisRef: Any,
			property: KProperty<*>,
			value: T
		) {
			this.value = value
			setter(key, value)
		}
	}

	/**
	 * Provides a delegate for a property that is backed by a preference.
	 *
	 * @param T
	 * @property key
	 * @property defaultValue
	 * @property getter
	 * @property setter
	 */
	protected class PreferenceProvider<T>(
		private val key: String?,
		private val defaultValue: T,
		private val getter: Getter<T>,
		private val setter: Setter<T>
	) {
		operator fun provideDelegate(
			thisRef: Any,
			property: KProperty<*>
		) = Preferences(key ?: property.name, defaultValue, getter, setter)
	}

	fun clear() = settings.clear()
}

@PublishedApi
internal inline fun <reified E : Enum<E>> com.russhwolf.settings.Settings.getEnum(
	key: String,
	defaultValue: E
): E {
	return enumEntries<E>()[(getInt(key, defaultValue.ordinal))]
}

@PublishedApi
internal inline fun <reified E : Enum<E>> com.russhwolf.settings.Settings.putEnum(
	key: String,
	value: E
) {
	putInt(key, value.ordinal)
}

class Settings(
	settings: com.russhwolf.settings.Settings
) : BasePreferenceManager(settings) {
	var useSystemFont by preference(false)
	var animatePlayerBackground by preference(true)
	var detachedBar by preference(true)
	var swipeToSkip by preference(true)
	var useShortNavbar by preference(false)
	var showProgressInBar by preference(true)
	var artGridRounding by preference(16f)
	var gridSize by preference(GridSize.TwoByTwo)
	var artGridItemSize by preference(150f)
	var useMarquee by preference(true)
	var marqueeSpeed by preference(MarqueeSpeed.Slow)
	var alphabeticalScroll by preference(false)
	var useWavySlider by preference(true)
	var lyricsAutoscroll by preference(true)
	var lyricsBeatByBeat by preference(true)
	var enableScrobbling by preference(true)
	var scrobblePercentage by preference(.5f)
	var minDurationToScrobble by preference(30f)
	var windowPlacement by preference(0)
	var windowPositionX by preference(100f)
	var windowPositionY by preference(100f)
	var windowSizeX by preference(800f)
	var windowSizeY by preference(600f)
	var listType by preference(ListType.ALPHABETICAL_BY_ARTIST)

	// theme related settings
	var theme by preference(Theme.Dynamic)
	var accentColourH by preference(0f)
	var accentColourS by preference(0f)
	var accentColourV by preference(1f)

	companion object {
		val shared = paige.navic.data.models.Settings(
			com.russhwolf.settings.Settings()
		)
	}

	enum class MarqueeSpeed(val value: Int) {
		Slow(6000),
		Medium(4000),
		Fast(1000)
	}

	/**
	 * Different grid sizes which the user can choose from.
	 * Applies to all grids across the app.
	 *
	 * @property value The grid size
	 * @property label The label for this size, to be seen in settings
	 */
	enum class GridSize(val value: Int, val label: String) {
		TwoByTwo(2, "2x2"),
		ThreeByThree(3, "3x3"),
		FourByFour(4, "4x4")
	}

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
			val isDark = isSystemInDarkTheme()
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
					isDark = isSystemInDarkTheme(),
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
}

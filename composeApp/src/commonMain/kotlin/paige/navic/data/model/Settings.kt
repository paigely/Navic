// Mostly taken from https://github.com/zt64/tau/blob/main/core/src/main/kotlin/dev/zt64/tau/domain/manager/PreferencesManager.kt
// Copyright (c) 2025 zt64
// SPDX-License-Identifier: GPL-3.0

package paige.navic.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import paige.subsonic.api.model.ListType
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
	var dynamicColour by preference(true)
	var staticPlayerBackground by preference(false)
	var detachedBar by preference(true)
	var swipeToSkip by preference(true)
	var accentColourH by preference(0f)
	var accentColourS by preference(0f)
	var accentColourV by preference(1f)
	var useShortNavbar by preference(false)
	var showProgressInBar by preference(true)
	var artGridRounding by preference(16f)
	var artGridItemsPerRow by preference(2)
	var artGridItemSize by preference(150f)
	var useMarquee by preference(true)
	var marqueeDuration by preference(4000)
	var useWavySlider by preference(true)
	var lyricsAutoscroll by preference(true)
	var lyricsBeatByBeat by preference(true)
	var scrobblePercentage by preference(.7f)
	var minDurationToScrobble by preference(60f)
	var windowPlacement by preference(0)
	var windowPositionX by preference(100f)
	var windowPositionY by preference(100f)
	var windowSizeX by preference(800f)
	var windowSizeY by preference(600f)
	var listType by preference(ListType.ALPHABETICAL_BY_ARTIST)
	companion object {
		val shared = paige.navic.data.model.Settings(
			com.russhwolf.settings.Settings()
		)
	}
}

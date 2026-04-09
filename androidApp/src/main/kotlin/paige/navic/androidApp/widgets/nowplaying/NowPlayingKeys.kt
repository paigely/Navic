package paige.navic.androidApp.widgets.nowplaying

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object NowPlayingKeys {
	val isPlaying = booleanPreferencesKey("is_playing")
	val titleKey = stringPreferencesKey("title")
	val artistKey = stringPreferencesKey("artist")
	val artUrlKey = stringPreferencesKey("art_url")
}

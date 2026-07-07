package paige.navic.androidApp.shared

import paige.navic.util.core.ResourceProvider

class AndroidResourceProvider(
	override val appIconDefault: Int = paige.navic.androidApp.R.mipmap.ic_launcher,
	override val appIconInverted: Int = paige.navic.androidApp.R.mipmap.ic_launcher_inverted,
	override val icNavic: Int = paige.navic.androidApp.R.drawable.ic_navic,
	override val animLibrary: Int = paige.navic.androidApp.R.drawable.anim_library,
	override val animPlaylist: Int = paige.navic.androidApp.R.drawable.anim_playlist,
	override val animArtist: Int = paige.navic.androidApp.R.drawable.anim_artist,
	override val animPause: Int = paige.navic.androidApp.R.drawable.anim_pause
) : ResourceProvider

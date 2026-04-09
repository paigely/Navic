package paige.navic.androidApp.shared

import paige.navic.shared.ResourceProvider

class AndroidResourceProvider(
	override val icNavic: Int = paige.navic.androidApp.R.drawable.ic_navic,
	override val animLibrary: Int = paige.navic.androidApp.R.drawable.anim_library,
	override val animPlaylist: Int = paige.navic.androidApp.R.drawable.anim_playlist,
	override val animArtist: Int = paige.navic.androidApp.R.drawable.anim_artist,
	override val animPause: Int = paige.navic.androidApp.R.drawable.anim_pause
) : ResourceProvider

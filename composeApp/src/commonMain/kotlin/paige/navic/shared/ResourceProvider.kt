package paige.navic.shared

// This class is a workaround for not being able to access :androidApp's R class inside :composeApp
interface ResourceProvider {
	val icNavic: Int
	val animLibrary: Int
	val animPlaylist: Int
	val animArtist: Int
	val animPause: Int
}

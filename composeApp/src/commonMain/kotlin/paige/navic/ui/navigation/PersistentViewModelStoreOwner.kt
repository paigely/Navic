package paige.navic.ui.navigation

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

/**
 * in-memory `ViewModel`s will be destroyed after the screen
 * they are associated with is removed from the back stack
 *
 * this might not be desirable for all `ViewModel`s, such as
 * the `LyricsScreenViewModel` and `ViewModel`s in non-nested
 * screens (e.g. tab screens), so this class can be passed
 * when using `koinViewModel` to selectively persist certain
 * `ViewModel`s when needed
 *
 * there's probably a better way to do this but this seems to
 * work fine
 */
class PersistentViewModelStoreOwner : ViewModelStoreOwner {
	override val viewModelStore = ViewModelStore()
}

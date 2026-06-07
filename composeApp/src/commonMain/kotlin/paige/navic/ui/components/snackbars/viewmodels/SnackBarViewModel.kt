package paige.navic.ui.components.snackbars.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import navic.composeapp.generated.resources.Res
import navic.composeapp.generated.resources.notice_added_to_queue
import navic.composeapp.generated.resources.notice_play_next
import org.jetbrains.compose.resources.StringResource
import paige.navic.domain.models.snackbars.PlayerEvent

class SnackBarViewModel: ViewModel() {
	private val _events = MutableSharedFlow<PlayerEvent>()
	val events: SharedFlow<PlayerEvent> = _events.asSharedFlow()

	fun notify(resource: StringResource, vararg args: Any) {
		viewModelScope.launch {
			_events.emit(PlayerEvent(resource, args.toList()))
		}
	}

	fun notifyAddedToQueue() = notify(Res.string.notice_added_to_queue)
	fun notifyPlayNext() = notify(Res.string.notice_play_next)
}

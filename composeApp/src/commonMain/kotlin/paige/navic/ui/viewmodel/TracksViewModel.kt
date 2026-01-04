package paige.navic.ui.viewmodel

import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.LibraryRepository
import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.AnyTrack
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

class TracksViewModel(
	private val repository: LibraryRepository = LibraryRepository()
) : ViewModel() {
	private val _selectedTrack = MutableStateFlow<AnyTrack?>(null)
	val selectedtrack: StateFlow<AnyTrack?> = _selectedTrack.asStateFlow()

	private val _error = MutableStateFlow<Exception?>(null)
	val error = _error.asStateFlow()

	fun selectTrack(track: AnyTrack) {
		_selectedTrack.value = track
	}

	fun clearSelection() {
		_selectedTrack.value = null
	}

	fun clearError() {
		_error.value = null
	}

	fun shareSelectedTrack(clipboard: ClipboardManager) {
		viewModelScope.launch {
			try {
				SessionManager.api.createShare(
					_selectedTrack.value?.id,
					"${Clock.System.now()
						.plus(1.hours)
						.toEpochMilliseconds()}"
				).data.shares.values.firstOrNull()?.firstOrNull()?.url?.let {
					clipboard.setText(
						AnnotatedString(it)
					)
				}
			} catch (e: Exception) {
				_error.value = e
			}
		}
	}
}
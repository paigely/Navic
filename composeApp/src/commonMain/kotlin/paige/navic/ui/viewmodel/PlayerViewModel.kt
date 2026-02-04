package paige.navic.ui.viewmodel

import androidx.compose.material3.rememberSliderState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import paige.navic.data.repository.LyricsRepository
import paige.navic.util.UiState
import paige.subsonic.api.model.Track
import kotlin.time.Duration

class PlayerViewModel : ViewModel() {

}
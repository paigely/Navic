package paige.navic.ui.screens.login.viewmodels

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import paige.navic.data.database.entities.ServerEntity
import paige.navic.data.models.User
import paige.navic.data.session.SessionManager
import paige.navic.domain.repositories.DbRepository
import paige.navic.domain.repositories.ServerRepository
import paige.navic.utils.LoginState

class LoginViewModel(
	private val serverRepository: ServerRepository,
	private val dbRepository: DbRepository
) : ViewModel() {

	private val _loginState = MutableStateFlow<LoginState<User?>>(LoginState.Idle)
	val loginState = _loginState.asStateFlow()

	val savedServers = serverRepository.allServers.stateIn(
		scope = viewModelScope,
		started = SharingStarted.WhileSubscribed(5000),
		initialValue = emptyList()
	)

	var selectedServer by mutableStateOf<ServerEntity?>(null)
		private set

	var isEditing by mutableStateOf(false)
		private set

	val serverNameState = TextFieldState()
	val instanceState = TextFieldState()
	val usernameState = TextFieldState()
	val passwordState = TextFieldState()

	var instanceError by mutableStateOf(false)
		private set
	var usernameError by mutableStateOf(false)
		private set
	var passwordError by mutableStateOf(false)
		private set

	init {
		loadUser()
	}

	fun selectServer(server: ServerEntity) {
		isEditing = false
		selectedServer = server
		serverNameState.setTextAndPlaceCursorAtEnd(server.name)
		instanceState.setTextAndPlaceCursorAtEnd(server.url)
		usernameState.setTextAndPlaceCursorAtEnd(server.username)
		passwordState.setTextAndPlaceCursorAtEnd(server.password)
		resetErrors()
		login()
	}

	fun editServer(server: ServerEntity) {
		selectedServer = server
		isEditing = true
		serverNameState.setTextAndPlaceCursorAtEnd(server.name)
		instanceState.setTextAndPlaceCursorAtEnd(server.url)
		usernameState.setTextAndPlaceCursorAtEnd(server.username)
		passwordState.setTextAndPlaceCursorAtEnd(server.password)
		resetErrors()
	}

	fun addNewServer() {
		selectedServer = null
		isEditing = false
		serverNameState.clearText()
		instanceState.clearText()
		usernameState.clearText()
		passwordState.clearText()
		resetErrors()
	}

	fun cancelEdit() {
		if (isEditing) {
			isEditing = false
		}
	}

	private fun resetErrors() {
		instanceError = false
		usernameError = false
		passwordError = false
	}

	fun validateInstance() {
		instanceError = instanceState.text.isBlank()
	}

	fun validateUsername() {
		usernameError = usernameState.text.isBlank()
	}

	fun validatePassword() {
		passwordError = passwordState.text.isBlank()
	}

	fun validateStuff(): Boolean {
		validateInstance()
		validateUsername()
		if (!isEditing && selectedServer != null && passwordState.text.isEmpty()) return true
		validatePassword()
		return !instanceError && !usernameError && !passwordError
	}

	fun loadUser() {
		viewModelScope.launch {
			val user = SessionManager.currentUser
			if (user != null) {
				_loginState.value = LoginState.Success(user)
			}
		}
	}

	fun login(): Boolean {
		if (!validateStuff()) return false

		viewModelScope.launch {
			_loginState.value = LoginState.Loading

			try {
				val rawUrl = instanceState.text.toString().trim()
				val url = if (!rawUrl.startsWith("https://") && !rawUrl.startsWith("http://")) {
					"https://$rawUrl"
				} else rawUrl

				val username = usernameState.text.toString()
				val password = passwordState.text.toString()

				SessionManager.login(url, username, password)

				val user = SessionManager.currentUser ?: throw Exception("Login failed")
				val serverId = SessionManager.activeServerId.value ?: url.hashCode().toString()

				val serverEntity = ServerEntity(
					serverId = selectedServer?.serverId ?: serverId,
					name = serverNameState.text.toString().ifBlank { "My Server" },
					url = url,
					username = username,
					password = password
				)

				serverRepository.upsertServer(serverEntity)
				serverRepository.updateLastUsed(serverEntity.serverId)

				dbRepository.syncEverything { progress, message ->
					_loginState.value = LoginState.Syncing(progress, message)
				}.onSuccess {
					_loginState.value = LoginState.Success(user)
				}.onFailure { e ->
					_loginState.value = LoginState.Error(e as Exception)
				}

			} catch (e: Exception) {
				_loginState.value = LoginState.Error(e)
			}
		}
		return true
	}

	fun deleteServer(server: ServerEntity) {
		viewModelScope.launch {
			serverRepository.deleteServerEntry(server.serverId)
			if (selectedServer?.serverId == server.serverId) {
				addNewServer()
			}
		}
	}

	fun logout() {
		_loginState.value = LoginState.Idle
		SessionManager.logout()
		viewModelScope.launch {
			dbRepository.removeEverything()
		}
	}
}

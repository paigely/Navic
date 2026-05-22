package paige.navic.data.session

import com.russhwolf.settings.set
import dev.zt64.subsonic.client.SubsonicAuth
import dev.zt64.subsonic.client.SubsonicClient
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import paige.navic.data.models.User
import paige.navic.data.models.settings.Settings
import com.russhwolf.settings.Settings as KmpSettings

object SessionManager {
	private val settings = KmpSettings()
	private val _isLoggedIn = MutableStateFlow(false)
	val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

	var api: SubsonicClient = createClient(
		instanceUrl = settings.getString("instanceUrl", ""),
		username = settings.getString("username", ""),
		password = settings.getString("password", ""),
	)
		private set

	private fun createClient(
		instanceUrl: String,
		username: String,
		password: String,
	) = SubsonicClient(
		baseUrl = instanceUrl,
		auth = SubsonicAuth.Token(
			username = username,
			password = password,
		),
		client = "Navic",
		clientConfig = {
			install(UserAgent) {
				agent = "Navic"
			}

			val customHeaders = Settings.shared.customHeadersMap()
			if (customHeaders.isNotEmpty()) {
				defaultRequest {
					customHeaders.forEach { (key, value) -> header(key, value) }
				}
			}
		}
	)

	val currentUser: User?
		get() {
			val username = settings.getStringOrNull("username") ?: return null

			_isLoggedIn.value = true

			return User(
				name = username,
				avatarUrl = api.getAvatarUrl(username)
			)
		}

	@OptIn(DelicateCoroutinesApi::class)
	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	) {
		val client = createClient(instanceUrl, username, password)

		try {
			client.ping()
		} catch (e: Exception) {
			throw Exception(
				"Failed to connect to the instance. Please check your credentials and try again.",
				e
			)
		}

		settings["instanceUrl"] = instanceUrl
		settings["username"] = username
		settings["password"] = password

		api = client
		_isLoggedIn.value = true
	}

	fun logout() {
		settings["username"] = null
		settings["password"] = null
		_isLoggedIn.value = false
	}

	fun refreshClient() {
		api = createClient(
			instanceUrl = settings.getString("instanceUrl", ""),
			username = settings.getString("username", ""),
			password = settings.getString("password", ""),
		)
	}
}

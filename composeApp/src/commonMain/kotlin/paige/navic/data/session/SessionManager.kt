package paige.navic.data.session

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import io.ktor.client.HttpClient
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import paige.navic.data.models.User
import paige.subsonic.api.AuthType
import paige.subsonic.api.SubsonicApi

object SessionManager {
	private val settings = Settings()
	private val _isLoggedIn = MutableStateFlow(false)
	val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

	// platform specified http cache storage
	var cacheStorage: CacheStorage? = null

	val api: SubsonicApi
		get() {
			return SubsonicApi(
				baseUrl = settings.getString("instanceUrl", ""),
				username = settings.getString("username", ""),
				password = settings.getString("password", ""),
				apiVersion = "1.16.1",
				clientId = "Navic",
				authType = AuthType.Token(),
				baseClient = HttpClient {
					install(HttpCache) {
						cacheStorage?.let {
							publicStorage(it)
						}
					}
				}
			)
		}

	val currentUser: User?
		get() {
			val username = settings.getStringOrNull("username")
				?.takeIf { it.isNotBlank() }
				?: return null
			_isLoggedIn.value = true
			return User(
				name = username,
				avatarUrl = api.avatarUrl(username, true)
			)
		}

	@OptIn(DelicateCoroutinesApi::class)
	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	) {
		settings["instanceUrl"] = instanceUrl
		settings["username"] = username
		settings["password"] = password

		try {
			api.getUser(username)
			_isLoggedIn.value = true
		} catch (e: Throwable) {
			throw e
		}
	}


	fun logout() {
		settings["username"] = null
		settings["password"] = null
		_isLoggedIn.value = false
	}
}

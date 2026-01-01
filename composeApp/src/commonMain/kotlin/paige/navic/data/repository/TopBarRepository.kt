package paige.navic.data.repository

import paige.navic.data.model.User
import paige.navic.data.session.SessionManager

class TopBarRepository {
	fun getLoggedInUser(): User? {
		return SessionManager.currentUser
	}

	suspend fun login(
		instanceUrl: String,
		username: String,
		password: String
	): User? {
		SessionManager.login(instanceUrl, username, password)
		return SessionManager.currentUser
	}

	fun logout() {
		SessionManager.logout()
	}
}

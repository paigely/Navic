package paige.navic.domain.repositories

import paige.navic.data.database.mappers.toDomainModel
import paige.navic.domain.manager.SessionManager

class ShareRepository(
	private val sessionManager: SessionManager
) {
	suspend fun getShares() = sessionManager.api.getShares().map { it.toDomainModel() }
}

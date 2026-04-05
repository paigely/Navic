package paige.navic.domain.repositories

import paige.navic.data.database.mappers.toDomainModel
import paige.navic.data.session.SessionManager

class ShareRepository {
	suspend fun getShares() = SessionManager.api.getShares().map { it.toDomainModel() }
}
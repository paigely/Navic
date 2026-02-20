package paige.navic.data.repositories

import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Share

class SharesRepository {
	suspend fun getShares(): List<Share> {
		return SessionManager.api
			.getShares()
			.data.shares.share.orEmpty()
	}
}
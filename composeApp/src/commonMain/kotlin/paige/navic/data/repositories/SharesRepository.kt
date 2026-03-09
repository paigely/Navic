package paige.navic.data.repositories

import dev.zt64.subsonic.api.model.Share
import paige.navic.data.session.SessionManager

class SharesRepository {
	suspend fun getShares(): List<Share> = SessionManager.api.getShares()
}
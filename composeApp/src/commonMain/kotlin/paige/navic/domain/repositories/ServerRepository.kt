package paige.navic.domain.repositories

import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.dao.ServerDao
import paige.navic.data.database.entities.ServerEntity
import kotlin.time.Clock

class ServerRepository(
	private val serverDao: ServerDao
) {
	val allServers: Flow<List<ServerEntity>> = serverDao.getAllServersFlow()

	suspend fun upsertServer(server: ServerEntity) {
		serverDao.insertServer(server)
	}

	suspend fun updateLastUsed(serverId: String) {
		serverDao.updateLastUsed(serverId, Clock.System.now().toEpochMilliseconds())
	}

	suspend fun deleteServerEntry(serverId: String) {
		serverDao.deleteServer(serverId)
	}
}

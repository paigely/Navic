package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.entities.PlaylistSongCrossRef
import paige.navic.data.database.relations.PlaylistWithSongs
import paige.navic.shared.Logger

@Dao
interface PlaylistDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlaylist(playlist: PlaylistEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlaylists(playlists: List<PlaylistEntity>)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertPlaylistSongCrossRefs(crossRefs: List<PlaylistSongCrossRef>)

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE serverId = :serverId ORDER BY name ASC")
	suspend fun getAllPlaylistsByName(serverId: String): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE serverId = :serverId ORDER BY createdAt DESC")
	suspend fun getAllPlaylistsByDateAdded(serverId: String): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE serverId = :serverId ORDER BY duration DESC")
	suspend fun getAllPlaylistsByDuration(serverId: String): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE serverId = :serverId ORDER BY RANDOM()")
	suspend fun getAllPlaylistsRandom(serverId: String): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE serverId = :serverId ORDER BY name ASC")
	fun getAllPlaylistsFlow(serverId: String): Flow<List<PlaylistWithSongs>>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE playlistId = :playlistId AND serverId = :serverId LIMIT 1")
	suspend fun getPlaylistById(playlistId: String, serverId: String): PlaylistWithSongs?

	@Query("DELETE FROM PlaylistEntity WHERE playlistId = :playlistId AND serverId = :serverId")
	suspend fun deletePlaylist(playlistId: String, serverId: String)

	@Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId")
	suspend fun deletePlaylistSongCrossRefs(playlistId: String)

	@Transaction
	suspend fun replacePlaylistSongs(playlistId: String, crossRefs: List<PlaylistSongCrossRef>) {
		deletePlaylistSongCrossRefs(playlistId)
		insertPlaylistSongCrossRefs(crossRefs)
	}

	@Query("SELECT COUNT(*) FROM PlaylistEntity WHERE serverId = :serverId")
	suspend fun getPlaylistCount(serverId: String): Int

	@Query("DELETE FROM PlaylistEntity WHERE serverId = :serverId")
	suspend fun clearAllPlaylistsForServer(serverId: String)

	@Query("SELECT playlistId FROM PlaylistEntity WHERE serverId = :serverId")
	suspend fun getAllPlaylistIds(serverId: String): List<String>

	@Transaction
	@Query("""
		SELECT PlaylistEntity.* FROM PlaylistEntity 
		JOIN PlaylistFts ON PlaylistEntity.rowid = PlaylistFts.rowid 
		WHERE serverId = :serverId AND PlaylistFts MATCH :query
	""")
	suspend fun searchPlaylistsList(query: String, serverId: String): List<PlaylistWithSongs>

	@Transaction
	suspend fun updateAllPlaylists(serverId: String, remotePlaylists: List<PlaylistEntity>) {
		val remoteIds = remotePlaylists.map { it.playlistId }.toSet()
		getAllPlaylistIds(serverId).forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("PlaylistDao", "playlist $localId no longer exists remotely")
				deletePlaylist(localId, serverId)
			}
		}
		insertPlaylists(remotePlaylists)
	}

	@Transaction
	suspend fun deleteObsoletePlaylists(serverId: String, remoteIds: Set<String>) {
		getAllPlaylistIds(serverId).forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("PlaylistDao", "playlist $localId no longer exists remotely")
				deletePlaylist(localId, serverId)
			}
		}
	}
}

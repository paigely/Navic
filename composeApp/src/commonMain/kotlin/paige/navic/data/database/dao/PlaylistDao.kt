package paige.navic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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
	@Query("SELECT * FROM PlaylistEntity ORDER BY name ASC")
	suspend fun getAllPlaylistsByName(): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity ORDER BY createdAt DESC")
	suspend fun getAllPlaylistsByDateAdded(): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity ORDER BY duration DESC")
	suspend fun getAllPlaylistsByDuration(): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity ORDER BY RANDOM()")
	suspend fun getAllPlaylistsRandom(): List<PlaylistWithSongs>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity ORDER BY name ASC")
	fun getAllPlaylistsFlow(): Flow<List<PlaylistWithSongs>>

	@Transaction
	@Query("SELECT * FROM PlaylistEntity WHERE playlistId = :playlistId LIMIT 1")
	suspend fun getPlaylistById(playlistId: String): PlaylistWithSongs?

	@Query("DELETE FROM PlaylistEntity WHERE playlistId = :playlistId")
	suspend fun deletePlaylist(playlistId: String)

	@Query("DELETE FROM PlaylistSongCrossRef WHERE playlistId = :playlistId")
	suspend fun deletePlaylistSongCrossRefs(playlistId: String)

	@Transaction
	suspend fun replacePlaylistSongs(playlistId: String, crossRefs: List<PlaylistSongCrossRef>) {
		deletePlaylistSongCrossRefs(playlistId)
		insertPlaylistSongCrossRefs(crossRefs)
	}

	@Query("SELECT COUNT(*) FROM PlaylistEntity")
	suspend fun getPlaylistCount(): Int

	@Query("DELETE FROM PlaylistEntity")
	suspend fun clearAllPlaylists()

	@Query("SELECT playlistId FROM PlaylistEntity")
	suspend fun getAllPlaylistIds(): List<String>

	@Transaction
	suspend fun updateAllPlaylists(remotePlaylists: List<PlaylistEntity>) {
		val remoteIds = remotePlaylists.map { it.playlistId }.toSet()
		getAllPlaylistIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("PlaylistDao", "playlist $localId no longer exists remotely")
				deletePlaylist(localId)
			}
		}
		insertPlaylists(remotePlaylists)
	}
}
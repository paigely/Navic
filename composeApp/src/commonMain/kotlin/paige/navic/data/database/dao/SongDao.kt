package paige.navic.data.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import paige.navic.data.database.entities.SongEntity
import paige.navic.shared.Logger

@Dao
interface SongDao {
	@Query("SELECT * FROM SongEntity WHERE songId = :songId AND serverId = :serverId LIMIT 1")
	suspend fun getSongById(songId: String, serverId: String): SongEntity?

	@Query("SELECT * FROM SongEntity WHERE serverId = :serverId ORDER BY title COLLATE NOCASE ASC")
	fun getAllSongsPaging(serverId: String): PagingSource<Int, SongEntity>

	@Query("SELECT * FROM SongEntity WHERE serverId = :serverId AND artistId = :artistId ORDER BY title COLLATE NOCASE ASC")
	fun getSongsByArtistPaging(artistId: String, serverId: String): PagingSource<Int, SongEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSong(song: SongEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSongs(songs: List<SongEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertSongsIgnoringConflicts(songs: List<SongEntity>)

	@Query("SELECT * FROM SongEntity WHERE serverId = :serverId")
	suspend fun getAllSongs(serverId: String): List<SongEntity>

	@Query("SELECT * FROM SongEntity WHERE belongsToAlbumId = :albumId AND serverId = :serverId")
	suspend fun getSongsByAlbumId(albumId: String, serverId: String): List<SongEntity>

	@Query("DELETE FROM SongEntity WHERE songId = :songId AND serverId = :serverId")
	suspend fun deleteSong(songId: String, serverId: String)

	// TODO
	@Query("SELECT EXISTS(SELECT 1 FROM SongEntity WHERE songId = :songId AND serverId = :serverId AND starredAt IS NOT NULL)")
	suspend fun isSongStarred(songId: String, serverId: String): Boolean

	@Query("SELECT userRating FROM SongEntity WHERE songId = :songId AND serverId = :serverId")
	suspend fun getSongRating(songId: String, serverId: String): Int?

	@Query("DELETE FROM SongEntity WHERE serverId = :serverId")
	suspend fun clearAllSongsForServer(serverId: String)

	@Query("SELECT songId FROM SongEntity WHERE serverId = :serverId")
	suspend fun getAllSongIds(serverId: String): List<String>

	@Query("SELECT * FROM SongEntity WHERE serverId = :serverId AND songId IN (:ids)")
	suspend fun getSongsByIds(ids: List<String>, serverId: String): List<SongEntity>

	@Query("""
		SELECT SongEntity.* FROM SongEntity 
		JOIN SongFts ON SongEntity.rowid = SongFts.rowid 
		WHERE serverId = :serverId AND SongFts MATCH :query
	""")
	suspend fun searchSongsList(query: String, serverId: String): List<SongEntity>

	@Transaction
	suspend fun updateSongsByAlbumId(albumId: String, serverId: String, remoteSongs: List<SongEntity>) {
		val remoteIds = remoteSongs.map { it.songId }.toSet()
		getSongsByAlbumId(albumId, serverId).forEach { localSong ->
			if (localSong.songId !in remoteIds) {
				Logger.w("SongDao", "song ${localSong.songId} no longer belongs to album $albumId")
				deleteSong(localSong.songId, serverId)
			}
		}
		insertSongs(remoteSongs)
	}

	@Transaction
	suspend fun updateAllSongs(serverId: String, remoteSongs: List<SongEntity>) {
		val remoteIds = remoteSongs.map { it.songId }.toSet()
		getAllSongIds(serverId).forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("SongDao", "song $localId no longer exists remotely")
				deleteSong(localId, serverId)
			}
		}
		insertSongs(remoteSongs)
	}

	@Query("DELETE FROM SongEntity WHERE serverId = :serverId AND songId IN (:ids)")
	suspend fun deleteSongs(serverId: String,ids: List<String>)

	@Transaction
	suspend fun deleteObsoleteSongs(serverId: String, remoteIds: Set<String>) {
		val localIds = getAllSongIds(serverId)
		val toDelete = localIds.filter { it !in remoteIds }
		if (toDelete.isNotEmpty()) {
			toDelete.chunked(900).forEach { chunk ->
				deleteSongs(serverId, chunk)
			}
		}
	}
}

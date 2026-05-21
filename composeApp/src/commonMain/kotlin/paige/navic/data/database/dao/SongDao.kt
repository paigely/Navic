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
	@Query("SELECT * FROM SongEntity WHERE songId = :songId LIMIT 1")
	suspend fun getSongById(songId: String): SongEntity?

	@Query("SELECT * FROM SongEntity ORDER BY title COLLATE NOCASE ASC")
	fun getAllSongsPaging(): PagingSource<Int, SongEntity>

	@Query("SELECT * FROM SongEntity WHERE artistId = :artistId ORDER BY title COLLATE NOCASE ASC")
	fun getSongsByArtistPaging(artistId: String): PagingSource<Int, SongEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSong(song: SongEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertSongs(songs: List<SongEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertSongsIgnoringConflicts(songs: List<SongEntity>)

	@Query("SELECT * FROM SongEntity")
	suspend fun getAllSongs(): List<SongEntity>

	@Query("SELECT * FROM SongEntity WHERE belongsToAlbumId = :albumId")
	suspend fun getSongsByAlbumId(albumId: String): List<SongEntity>

	@Query("DELETE FROM SongEntity WHERE songId = :songId")
	suspend fun deleteSong(songId: String)

	// TODO
	@Query("SELECT EXISTS(SELECT 1 FROM SongEntity WHERE songId = :songId AND starredAt IS NOT NULL)")
	suspend fun isSongStarred(songId: String): Boolean

	@Query("SELECT userRating FROM SongEntity WHERE songId = :songId")
	suspend fun getSongRating(songId: String): Int?

	@Query("DELETE FROM SongEntity")
	suspend fun clearAllSongs()

	@Query("SELECT songId FROM SongEntity")
	suspend fun getAllSongIds(): List<String>

	@Query("SELECT * FROM SongEntity WHERE songId IN (:ids)")
	suspend fun getSongsByIds(ids: List<String>): List<SongEntity>

	@Query("""
		SELECT SongEntity.* FROM SongEntity 
		JOIN SongFts ON SongEntity.rowid = SongFts.rowid 
		WHERE SongFts MATCH :query
	""")
	suspend fun searchSongsList(query: String): List<SongEntity>

	@Transaction
	suspend fun updateSongsByAlbumId(albumId: String, remoteSongs: List<SongEntity>) {
		val remoteIds = remoteSongs.map { it.songId }.toSet()
		getSongsByAlbumId(albumId).forEach { localSong ->
			if (localSong.songId !in remoteIds) {
				Logger.w("SongDao", "song ${localSong.songId} no longer belongs to album $albumId")
				deleteSong(localSong.songId)
			}
		}
		insertSongs(remoteSongs)
	}

	@Transaction
	suspend fun updateAllSongs(remoteSongs: List<SongEntity>) {
		val remoteIds = remoteSongs.map { it.songId }.toSet()
		getAllSongIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("SongDao", "song $localId no longer exists remotely")
				deleteSong(localId)
			}
		}
		insertSongs(remoteSongs)
	}

	@Query("DELETE FROM SongEntity WHERE songId IN (:ids)")
	suspend fun deleteSongs(ids: List<String>)

	@Transaction
	suspend fun deleteObsoleteSongs(remoteIds: Set<String>) {
		val localIds = getAllSongIds()
		val toDelete = localIds.filter { it !in remoteIds }
		if (toDelete.isNotEmpty()) {
			toDelete.chunked(900).forEach { chunk ->
				deleteSongs(chunk)
			}
		}
	}
}

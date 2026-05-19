package paige.navic.data.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.RawQuery
import androidx.room3.RoomRawQuery
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.AlbumEntity
import paige.navic.data.database.relations.AlbumWithSongs
import paige.navic.shared.Logger

@Dao
interface AlbumDao {
	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId AND (genre = :genreName OR genres LIKE '%' || :genreName || '%') ORDER BY year DESC, name COLLATE NOCASE ASC")
	fun getAlbumsByGenre(genreName: String, serverId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY name ASC")
	fun getAllAlbums(serverId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY name ASC")
	suspend fun getAllAlbumsList(serverId: String): List<AlbumWithSongs>

	@RawQuery
	suspend fun getAlbumsByQuery(query: RoomRawQuery): List<AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY name ASC")
	fun getAlbumsByNameAsc(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY name DESC")
	fun getAlbumsByNameDesc(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId AND starredAt IS NOT NULL ORDER BY starredAt DESC")
	fun getStarredAlbums(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY artistName COLLATE NOCASE ASC, year DESC")
	fun getAlbumsByArtistAsc(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY artistName COLLATE NOCASE DESC, year DESC")
	fun getAlbumsByArtistDesc(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT albumId FROM AlbumEntity WHERE serverId = :serverId ORDER BY RANDOM()")
	suspend fun getRandomAlbumIds(serverId: String): List<String>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY createdAt DESC")
	fun getAlbumsNewest(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY createdAt ASC")
	fun getAlbumsOldest(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY playCount DESC")
	fun getAlbumsFrequent(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY playCount ASC")
	fun getAlbumsInfrequent(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY lastPlayedAt DESC")
	fun getAlbumsRecent(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId ORDER BY lastPlayedAt ASC")
	fun getAlbumsStale(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("""
    SELECT * FROM AlbumEntity 
    WHERE serverId = :serverId AND albumId IN (
        SELECT song.belongsToAlbumId FROM SongEntity AS song
        INNER JOIN DownloadEntity AS dl ON song.songId = dl.songId
        WHERE dl.status = 'DOWNLOADED'
    )
    ORDER BY name ASC
    """)
	fun getDownloadedAlbums(serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE genre = :genreName OR genres LIKE '%' || :genreName || '%' ORDER BY year DESC, name COLLATE NOCASE ASC")
	fun getAlbumsByGenre(genreName: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE genre = :genreName OR genres LIKE '%' || :genreName || '%' ORDER BY year ASC, name COLLATE NOCASE DESC")
	fun getAlbumsByGenreReversed(genreName: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT COUNT(albumId) FROM AlbumEntity WHERE serverId = :serverId")
	suspend fun getAlbumCount(serverId: String): Int

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE albumId = :albumId AND serverId = :serverId LIMIT 1")
	suspend fun getAlbumById(albumId: String, serverId: String): AlbumWithSongs?

	@Query("SELECT EXISTS(SELECT 1 FROM AlbumEntity WHERE albumId = :albumId AND serverId = :serverId AND starredAt IS NOT NULL)")
	suspend fun isAlbumStarred(albumId: String, serverId: String): Boolean

	@Query("SELECT userRating FROM AlbumEntity WHERE albumId = :albumId AND serverId = :serverId")
	suspend fun getAlbumRating(albumId: String, serverId: String): Int?

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId AND serverId = :serverId ORDER BY year DESC")
	fun getAlbumsByArtist(artistId: String, serverId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId AND serverId = :serverId ORDER BY year DESC")
	fun getAlbumsByArtistPaging(artistId: String, serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId AND albumId != :albumId AND serverId = :serverId ORDER BY year DESC")
	fun getAlbumsByArtistExcluding(artistId: String, albumId: String, serverId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId AND albumId != :albumId AND serverId = :serverId ORDER BY year DESC")
	fun getAlbumsByArtistExcludingPaging(artistId: String, albumId: String, serverId: String): PagingSource<Int, AlbumWithSongs>

	@Transaction
	@Query("""
		SELECT AlbumEntity.* FROM AlbumEntity 
		JOIN AlbumFts ON AlbumEntity.rowid = AlbumFts.rowid 
		WHERE AlbumFts MATCH :query AND serverId = :serverId
	""")
	suspend fun searchAlbumsList(query: String, serverId: String): List<AlbumWithSongs>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAlbum(album: AlbumEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAlbums(albums: List<AlbumEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertAlbumsIgnoringConflicts(albums: List<AlbumEntity>)

	@Query("DELETE FROM AlbumEntity WHERE albumId = :albumId AND serverId = :serverId")
	suspend fun deleteAlbum(albumId: String, serverId: String)

	@Query("DELETE FROM AlbumEntity WHERE serverId = :serverId")
	suspend fun clearAllAlbumsForServer(serverId: String)

	@Query("SELECT albumId FROM AlbumEntity WHERE serverId = :serverId")
	suspend fun getAllAlbumIds(serverId: String): List<String>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE serverId = :serverId AND albumId IN (:ids)")
	suspend fun getAlbumsByIds(ids: List<String>, serverId: String): List<AlbumWithSongs>

	@Transaction
	suspend fun updateAllAlbums(serverId: String, remoteAlbums: List<AlbumEntity>) {
		val remoteIds = remoteAlbums.map { it.albumId }.toSet()
		getAllAlbumIds(serverId).forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("AlbumDao", "album $localId no longer exists remotely")
				deleteAlbum(localId, serverId)
			}
		}
		insertAlbums(remoteAlbums)
	}

	@Query("DELETE FROM AlbumEntity WHERE serverId = :serverId AND albumId IN (:ids)")
	suspend fun deleteAlbums(serverId: String, ids: List<String>)

	@Transaction
	suspend fun deleteObsoleteAlbums(serverId: String, remoteIds: Set<String>) {
		val localIds = getAllAlbumIds(serverId)
		val toDelete = localIds.filter { it !in remoteIds }
		if (toDelete.isNotEmpty()) {
			toDelete.chunked(900).forEach { chunk ->
				deleteAlbums(serverId, chunk)
			}
		}
	}
}

package paige.navic.data.database.dao

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
	@Query(" SELECT * FROM AlbumEntity WHERE genre = :genreName OR genres LIKE '%' || :genreName || '%' ORDER BY year DESC, name COLLATE NOCASE ASC")
	fun getAlbumsByGenre(genreName: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity ORDER BY name ASC")
	fun getAllAlbums(): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity ORDER BY name ASC")
	suspend fun getAllAlbumsList(): List<AlbumWithSongs>

	@RawQuery
	suspend fun getAlbumsByQuery(query: RoomRawQuery): List<AlbumWithSongs>

	@Transaction
	@Query("SELECT COUNT(albumId) FROM AlbumEntity")
	suspend fun getAlbumCount(): Int

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE albumId = :albumId LIMIT 1")
	suspend fun getAlbumById(albumId: String): AlbumWithSongs?

	@Query("SELECT EXISTS(SELECT 1 FROM AlbumEntity WHERE albumId = :albumId AND starredAt IS NOT NULL)")
	suspend fun isAlbumStarred(albumId: String): Boolean

	@Query("SELECT userRating FROM AlbumEntity WHERE albumId = :albumId")
	suspend fun getAlbumRating(albumId: String): Int?

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId ORDER BY year DESC")
	fun getAlbumsByArtist(artistId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE artistId = :artistId AND albumId != :albumId ORDER BY year DESC")
	fun getAlbumsByArtistExcluding(artistId: String, albumId: String): Flow<List<AlbumWithSongs>>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
	suspend fun searchAlbumsList(query: String): List<AlbumWithSongs>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAlbum(album: AlbumEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAlbums(albums: List<AlbumEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertAlbumsIgnoringConflicts(albums: List<AlbumEntity>)

	@Query("DELETE FROM AlbumEntity WHERE albumId = :albumId")
	suspend fun deleteAlbum(albumId: String)

	@Query("DELETE FROM AlbumEntity")
	suspend fun clearAllAlbums()

	@Query("SELECT albumId FROM AlbumEntity")
	suspend fun getAllAlbumIds(): List<String>

	@Transaction
	@Query("SELECT * FROM AlbumEntity WHERE albumId IN (:ids)")
	suspend fun getAlbumsByIds(ids: List<String>): List<AlbumWithSongs>

	@Transaction
	suspend fun updateAllAlbums(remoteAlbums: List<AlbumEntity>) {
		val remoteIds = remoteAlbums.map { it.albumId }.toSet()
		getAllAlbumIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("AlbumDao", "album $localId no longer exists remotely")
				deleteAlbum(localId)
			}
		}
		insertAlbums(remoteAlbums)
	}

	@Transaction
	suspend fun deleteObsoleteAlbums(remoteIds: Set<String>) {
		getAllAlbumIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("AlbumDao", "album $localId no longer exists remotely")
				deleteAlbum(localId)
			}
		}
	}
}

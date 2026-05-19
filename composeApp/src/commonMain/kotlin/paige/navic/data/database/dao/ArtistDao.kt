package paige.navic.data.database.dao

import androidx.paging.PagingSource
import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.ArtistEntity

@Dao
interface ArtistDao {
	@Query("SELECT COUNT(*) FROM ArtistEntity")
	fun getArtistsCountFlow(): Flow<Int>

	@Query("SELECT COUNT(*) FROM ArtistEntity WHERE starredAt IS NOT NULL")
	fun getStarredArtistsCountFlow(): Flow<Int>

	@Query("SELECT * FROM ArtistEntity ORDER BY name COLLATE NOCASE ASC")
	fun getArtistsAlphabeticalByNamePaging(): PagingSource<Int, ArtistEntity>

	@Query("SELECT * FROM ArtistEntity ORDER BY RANDOM()")
	fun getArtistsRandomPaging(): PagingSource<Int, ArtistEntity>

	@Query("SELECT * FROM ArtistEntity WHERE starredAt IS NOT NULL ORDER BY starredAt DESC")
	fun getArtistsStarredPaging(): PagingSource<Int, ArtistEntity>

	@Query("SELECT * FROM ArtistEntity ORDER BY name COLLATE NOCASE ASC")
	fun getAllArtists(): Flow<List<ArtistEntity>>

	@Query("SELECT * FROM ArtistEntity")
	suspend fun getAllArtistsList(): List<ArtistEntity>

	@Query("SELECT * FROM ArtistEntity WHERE artistId = :artistId LIMIT 1")
	suspend fun getArtistById(artistId: String): ArtistEntity?

	@Query("SELECT EXISTS(SELECT 1 FROM ArtistEntity WHERE artistId = :artistId AND starredAt IS NOT NULL)")
	suspend fun isArtistStarred(artistId: String): Boolean

	@Query("""
		SELECT ArtistEntity.* FROM ArtistEntity 
		JOIN ArtistFts ON ArtistEntity.rowid = ArtistFts.rowid 
		WHERE ArtistFts MATCH :query
	""")
	suspend fun searchArtistsList(query: String): List<ArtistEntity>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertArtist(artist: ArtistEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertArtists(artists: List<ArtistEntity>)

	@Insert(onConflict = OnConflictStrategy.IGNORE)
	suspend fun insertArtistsIgnoringConflicts(artists: List<ArtistEntity>)

	@Query("DELETE FROM ArtistEntity WHERE artistId = :artistId")
	suspend fun deleteArtist(artistId: String)

	@Query("DELETE FROM ArtistEntity")
	suspend fun clearAllArtists()

	@Query("SELECT artistId FROM ArtistEntity")
	suspend fun getAllArtistIds(): List<String>

	@Query("SELECT * FROM ArtistEntity WHERE artistId IN (:ids)")
	suspend fun getArtistsByIds(ids: List<String>): List<ArtistEntity>

	@Query("DELETE FROM ArtistEntity WHERE artistId IN (:ids)")
	suspend fun deleteArtists(ids: List<String>)

	@Transaction
	suspend fun deleteObsoleteArtists(remoteIds: Set<String>) {
		val localIds = getAllArtistIds()
		val toDelete = localIds.filter { it !in remoteIds }
		if (toDelete.isNotEmpty()) {
			toDelete.chunked(900).forEach { chunk ->
				deleteArtists(chunk)
			}
		}
	}

	@Transaction
	suspend fun updateAllArtists(remoteArtists: List<ArtistEntity>) {
		val remoteIds = remoteArtists.map { it.artistId }.toSet()
		deleteObsoleteArtists(remoteIds)
		insertArtists(remoteArtists)
	}
}

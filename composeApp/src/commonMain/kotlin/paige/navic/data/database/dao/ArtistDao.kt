package paige.navic.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.ArtistEntity
import paige.navic.shared.Logger

@Dao
interface ArtistDao {
	@Query("SELECT * FROM ArtistEntity ORDER BY name COLLATE NOCASE ASC")
	suspend fun getArtistsAlphabeticalByName(): List<ArtistEntity>

	@Query("SELECT * FROM ArtistEntity ORDER BY RANDOM()")
	suspend fun getArtistsRandom(): List<ArtistEntity>

	@Query("SELECT * FROM ArtistEntity WHERE starredAt IS NOT NULL ORDER BY starredAt DESC")
	suspend fun getArtistsStarred(): List<ArtistEntity>

	@Query("SELECT * FROM ArtistEntity ORDER BY name COLLATE NOCASE ASC")
	fun getAllArtists(): Flow<List<ArtistEntity>>

	@Query("SELECT * FROM ArtistEntity")
	suspend fun getAllArtistsList(): List<ArtistEntity>

	@Query("SELECT * FROM ArtistEntity WHERE artistId = :artistId LIMIT 1")
	suspend fun getArtistById(artistId: String): ArtistEntity?

	@Query("SELECT EXISTS(SELECT 1 FROM ArtistEntity WHERE artistId = :artistId AND starredAt IS NOT NULL)")
	suspend fun isArtistStarred(artistId: String): Boolean

	@Query("SELECT * FROM ArtistEntity WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
	fun searchArtists(query: String): Flow<List<ArtistEntity>>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertArtist(artist: ArtistEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertArtists(artists: List<ArtistEntity>)

	@Query("DELETE FROM ArtistEntity WHERE artistId = :artistId")
	suspend fun deleteArtist(artistId: String)

	@Query("DELETE FROM ArtistEntity")
	suspend fun clearAllArtists()

	@Query("SELECT artistId FROM ArtistEntity")
	suspend fun getAllArtistIds(): List<String>

	@Transaction
	suspend fun updateAllArtists(remoteArtists: List<ArtistEntity>) {
		val remoteIds = remoteArtists.map { it.artistId }.toSet()
		getAllArtistIds().forEach { localId ->
			if (localId !in remoteIds) {
				Logger.w("ArtistDao", "artist $localId no longer exists remotely")
				deleteArtist(localId)
			}
		}
		insertArtists(remoteArtists)
	}
}
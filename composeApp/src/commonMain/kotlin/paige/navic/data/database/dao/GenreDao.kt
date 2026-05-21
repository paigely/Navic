package paige.navic.data.database.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow
import paige.navic.data.database.entities.GenreEntity
import paige.navic.data.database.relations.GenreWithAlbums

@Dao
interface GenreDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertGenre(song: GenreEntity)

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertGenres(genres: List<GenreEntity>)

	@Query("DELETE FROM GenreEntity WHERE genreName = :genreName")
	suspend fun deleteGenre(genreName: String)

	@Transaction
	@Query("SELECT * FROM GenreEntity ORDER BY albumCount DESC")
	suspend fun getGenres(): List<GenreEntity>

	@Transaction
	@Query("SELECT * FROM GenreEntity ORDER BY albumCount DESC")
	fun getGenresFlow(): Flow<List<GenreEntity>>

	@Transaction
	@Query("SELECT * FROM GenreEntity ORDER BY albumCount DESC")
	suspend fun getGenresWithAlbums(): List<GenreWithAlbums>

	@Transaction
	@Query("SELECT * FROM GenreEntity ORDER BY albumCount DESC")
	fun getGenresWithAlbumsFlow(): Flow<List<GenreWithAlbums>>

	@Query("DELETE FROM GenreEntity")
	suspend fun clearAllGenres()

	@Query("SELECT genreName FROM GenreEntity")
	suspend fun getAllGenreNames(): List<String>

	@Query("DELETE FROM GenreEntity WHERE genreName IN (:names)")
	suspend fun deleteGenres(names: List<String>)

	@Transaction
	suspend fun deleteObsoleteGenres(remoteNames: Set<String>) {
		val localNames = getAllGenreNames()
		val toDelete = localNames.filter { it !in remoteNames }
		if (toDelete.isNotEmpty()) {
			toDelete.chunked(900).forEach { chunk ->
				deleteGenres(chunk)
			}
		}
	}

	@Transaction
	suspend fun updateAllGenres(remoteGenres: List<GenreEntity>) {
		val remoteNames = remoteGenres.map { it.genreName }.toSet()
		deleteObsoleteGenres(remoteNames)
		insertGenres(remoteGenres)
	}
}

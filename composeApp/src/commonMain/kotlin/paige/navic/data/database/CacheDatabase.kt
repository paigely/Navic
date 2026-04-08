package paige.navic.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.dao.SyncActionDao
import paige.navic.data.database.entities.AlbumEntity
import paige.navic.data.database.entities.ArtistEntity
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.GenreEntity
import paige.navic.data.database.entities.LyricEntity
import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.entities.PlaylistSongCrossRef
import paige.navic.data.database.entities.SongEntity
import paige.navic.data.database.entities.SyncActionEntity

@Database(
	version = 2,
	entities = [
		AlbumEntity::class,
		GenreEntity::class,
		PlaylistEntity::class,
		PlaylistSongCrossRef::class,
		SongEntity::class,
		ArtistEntity::class,
		LyricEntity::class,
		SyncActionEntity::class,
		DownloadEntity::class
	]
)
@TypeConverters(Converters::class)
@ConstructedBy(CacheDatabaseConstructor::class)
abstract class CacheDatabase : RoomDatabase() {
	abstract fun albumDao(): AlbumDao
	abstract fun genreDao(): GenreDao
	abstract fun playlistDao(): PlaylistDao
	abstract fun songDao(): SongDao
	abstract fun artistDao(): ArtistDao
	abstract fun lyricDao(): LyricDao
	abstract fun syncActionDao(): SyncActionDao
	abstract fun downloadDao(): DownloadDao
}

@Suppress("KotlinNoActualForExpect")
expect object CacheDatabaseConstructor : RoomDatabaseConstructor<CacheDatabase> {
	override fun initialize(): CacheDatabase
}

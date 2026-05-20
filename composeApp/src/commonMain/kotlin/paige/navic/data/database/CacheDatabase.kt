package paige.navic.data.database

import androidx.room3.ConstructedBy
import androidx.room3.DaoReturnTypeConverters
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters
import androidx.room3.paging.PagingSourceDaoReturnTypeConverter
import paige.navic.data.database.dao.AlbumDao
import paige.navic.data.database.dao.ArtistDao
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.dao.GenreDao
import paige.navic.data.database.dao.LyricDao
import paige.navic.data.database.dao.PlayerStateDao
import paige.navic.data.database.dao.PlaylistDao
import paige.navic.data.database.dao.RadioDao
import paige.navic.data.database.dao.ServerDao
import paige.navic.data.database.dao.SongDao
import paige.navic.data.database.dao.SyncActionDao
import paige.navic.data.database.entities.AlbumEntity
import paige.navic.data.database.entities.AlbumFts
import paige.navic.data.database.entities.ArtistEntity
import paige.navic.data.database.entities.ArtistFts
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.GenreEntity
import paige.navic.data.database.entities.LyricEntity
import paige.navic.data.database.entities.PlayerStateEntity
import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.entities.PlaylistFts
import paige.navic.data.database.entities.PlaylistSongCrossRef
import paige.navic.data.database.entities.QueueItemEntity
import paige.navic.data.database.entities.RadioEntity
import paige.navic.data.database.entities.ServerEntity
import paige.navic.data.database.entities.SongEntity
import paige.navic.data.database.entities.SongFts
import paige.navic.data.database.entities.SyncActionEntity

@Database(
	version = 10,
	entities = [
		AlbumEntity::class,
		AlbumFts::class,
		GenreEntity::class,
		PlaylistEntity::class,
		PlaylistFts::class,
		PlaylistSongCrossRef::class,
		SongEntity::class,
		SongFts::class,
		ArtistEntity::class,
		ArtistFts::class,
		RadioEntity::class,
		LyricEntity::class,
		SyncActionEntity::class,
		DownloadEntity::class,
		ServerEntity::class,
		PlayerStateEntity::class,
		QueueItemEntity::class
	]
)
@TypeConverters(Converters::class)
@DaoReturnTypeConverters(PagingSourceDaoReturnTypeConverter::class)
@ConstructedBy(CacheDatabaseConstructor::class)
abstract class CacheDatabase : RoomDatabase() {
	abstract fun albumDao(): AlbumDao
	abstract fun genreDao(): GenreDao
	abstract fun downloadDao(): DownloadDao
	abstract fun playlistDao(): PlaylistDao
	abstract fun songDao(): SongDao
	abstract fun artistDao(): ArtistDao
	abstract fun radioDao(): RadioDao
	abstract fun lyricDao(): LyricDao
	abstract fun syncActionDao(): SyncActionDao
	abstract fun serverDao(): ServerDao
	abstract fun playerStateDao(): PlayerStateDao
}

expect object CacheDatabaseConstructor : RoomDatabaseConstructor<CacheDatabase> {
	override fun initialize(): CacheDatabase
}

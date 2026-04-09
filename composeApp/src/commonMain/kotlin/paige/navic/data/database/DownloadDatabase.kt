package paige.navic.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.entities.DownloadEntity

@Database(
	version = 1,
	entities = [DownloadEntity::class]
)
@ConstructedBy(DownloadDatabaseConstructor::class)
abstract class DownloadDatabase : RoomDatabase() {
	abstract fun downloadDao(): DownloadDao
}

@Suppress("KotlinNoActualForExpect")
expect object DownloadDatabaseConstructor : RoomDatabaseConstructor<DownloadDatabase> {
	override fun initialize(): DownloadDatabase
}


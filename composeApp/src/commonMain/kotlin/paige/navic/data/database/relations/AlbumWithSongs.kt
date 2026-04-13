package paige.navic.data.database.relations

import androidx.room3.Embedded
import androidx.room3.Relation
import paige.navic.data.database.entities.AlbumEntity
import paige.navic.data.database.entities.SongEntity

data class AlbumWithSongs(
	@Embedded val album: AlbumEntity,
	@Relation(
		parentColumn = "albumId",
		entityColumn = "belongsToAlbumId"
	)
	val songs: List<SongEntity>
)

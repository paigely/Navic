package paige.navic.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import paige.navic.data.database.entities.AlbumEntity
import paige.navic.data.database.entities.GenreEntity

data class GenreWithAlbums(
	@Embedded val genre: GenreEntity,
	@Relation(
		entity = AlbumEntity::class,
		parentColumn = "genreName",
		entityColumn = "genre"
	)
	val albums: List<AlbumWithSongs>
)
package paige.navic.data.database.relations

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.entities.PlaylistSongCrossRef
import paige.navic.data.database.entities.SongEntity

data class PlaylistWithSongs(
	@Embedded val playlist: PlaylistEntity,
	@Relation(
		parentColumn = "playlistId",
		entityColumn = "songId",
		associateBy = Junction(PlaylistSongCrossRef::class)
	)
	val songs: List<SongEntity>
)

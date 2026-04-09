package paige.navic.data.database.relations

import androidx.room.Embedded
import androidx.room.Relation
import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.entities.PlaylistSongCrossRef
import paige.navic.data.database.entities.SongEntity

data class PlaylistWithSongs(
	@Embedded val playlist: PlaylistEntity,
	@Relation(
		entity = PlaylistSongCrossRef::class,
		parentColumn = "playlistId",
		entityColumn = "playlistId"
	)
	val songs: List<PlaylistSong>
)

data class PlaylistSong(
	@Embedded val crossRef: PlaylistSongCrossRef,
	@Relation(
		parentColumn = "songId",
		entityColumn = "songId"
	)
	val song: SongEntity
)

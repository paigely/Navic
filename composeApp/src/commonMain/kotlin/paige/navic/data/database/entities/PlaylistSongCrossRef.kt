package paige.navic.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
	primaryKeys = ["playlistId", "songId", "position"],
	foreignKeys = [
		ForeignKey(
			entity = PlaylistEntity::class,
			parentColumns = ["playlistId"],
			childColumns = ["playlistId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = SongEntity::class,
			parentColumns = ["songId"],
			childColumns = ["songId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class PlaylistSongCrossRef(
	val playlistId: String,
	val songId: String,
	val position: Int
)

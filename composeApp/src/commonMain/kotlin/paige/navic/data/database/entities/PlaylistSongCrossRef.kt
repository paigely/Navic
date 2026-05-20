package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
	primaryKeys = ["serverId", "playlistId", "songId", "position"],
	indices = [Index(value = ["serverId", "songId"])],
	foreignKeys = [
		ForeignKey(
			entity = PlaylistEntity::class,
			parentColumns = ["serverId", "playlistId"],
			childColumns = ["serverId", "playlistId"],
			onDelete = ForeignKey.CASCADE
		),
		ForeignKey(
			entity = SongEntity::class,
			parentColumns = ["serverId", "songId"],
			childColumns = ["serverId", "songId"],
			onDelete = ForeignKey.CASCADE
		)
	]
)
data class PlaylistSongCrossRef(
	val serverId: String,
	val playlistId: String,
	val songId: String,
	val position: Int
)

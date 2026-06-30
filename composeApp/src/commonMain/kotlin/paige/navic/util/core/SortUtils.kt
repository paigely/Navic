package paige.navic.util.core

import androidx.room3.RoomRawQuery
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import paige.navic.data.database.entities.DownloadEntity
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType

// TODO: sort with sql instead
fun ImmutableList<DomainSong>.sortedByListType(
	listType: DomainSongListType,
	downloads: List<DownloadEntity>,
	albums: List<DomainAlbum>
): ImmutableList<DomainSong> {
	return when (listType) {
		DomainSongListType.FrequentlyPlayed -> sortedByDescending { it.playCount }
		DomainSongListType.Newest -> sortedByDescending {
			albums
				.firstOrNull { album -> album.id == it.albumId }
				?.createdAt
		}

		DomainSongListType.Starred -> filter { it.starredAt != null }.sortedBy { it.starredAt }
		DomainSongListType.Random -> shuffled()
		DomainSongListType.Downloaded -> filter { song ->
			downloads
				.filter { it.status == DownloadStatus.DOWNLOADED }
				.any { it.songId == song.id }
		}

		DomainSongListType.Rating -> sortedByDescending { it.userRating ?: 0 }
		DomainSongListType.Year -> sortedByDescending { it.year }
	}.toImmutableList()
}

fun DomainAlbumListType.toSqlQuery(): RoomRawQuery {
	var where: String? = null
	var orderBy: String
	val args = mutableListOf<Any>()

	when (this) {
		DomainAlbumListType.AlphabeticalByArtist -> orderBy = "LOWER(artistName) ASC"
		DomainAlbumListType.AlphabeticalByName -> orderBy = "LOWER(name) ASC"
		DomainAlbumListType.Frequent -> {
			where = "playCount != 0"
			orderBy = "playCount DESC"
		}

		DomainAlbumListType.Highest -> orderBy = "userRating DESC"
		DomainAlbumListType.Newest -> orderBy = "createdAt DESC"
		DomainAlbumListType.Random -> orderBy = "RANDOM()"
		DomainAlbumListType.Downloaded,
		DomainAlbumListType.Recent -> orderBy = "lastPlayedAt DESC"

		DomainAlbumListType.Starred -> {
			where = "starredAt IS NOT NULL"
			orderBy = "starredAt ASC"
		}

		is DomainAlbumListType.ByGenre -> {
			where = "genre = ?"
			orderBy = "LOWER(name) ASC"
			args.add(genre)
		}

		is DomainAlbumListType.ByYear -> {
			if (fromYear != null && toYear != null) {
				where = "COALESCE(year, 0) BETWEEN ? AND ?"
				orderBy = "LOWER(name) ASC"
				args.add(fromYear)
				args.add(toYear)
			} else {
				orderBy = "year DESC"
			}
		}
	}

	val whereClause = where?.let { " WHERE $it" } ?: ""
	val sql = "SELECT * FROM AlbumEntity$whereClause ORDER BY $orderBy"

	return RoomRawQuery(sql) { statement ->
		args.forEachIndexed { index, arg ->
			val bindIndex = index + 1
			when (arg) {
				is String -> statement.bindText(bindIndex, arg)
				is Int -> statement.bindInt(bindIndex, arg)
				is Long -> statement.bindLong(bindIndex, arg)
				is Float -> statement.bindFloat(bindIndex, arg)
				is Double -> statement.bindDouble(bindIndex, arg)
				is Boolean -> statement.bindInt(bindIndex, if (arg) 1 else 0)
			}
		}
	}
}

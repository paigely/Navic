package paige.navic.utils

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
	}.toImmutableList()
}

fun DomainAlbumListType.toSqlQuery(): RoomRawQuery {
	val (where, orderBy) = when (this) {
		DomainAlbumListType.AlphabeticalByArtist ->
			null to "LOWER(artistName) ASC"
		DomainAlbumListType.AlphabeticalByName ->
			null to "LOWER(name) ASC"
		DomainAlbumListType.Frequent ->
			"playCount != 0" to "playCount DESC"
		DomainAlbumListType.Highest ->
			null to "userRating DESC"
		DomainAlbumListType.Newest ->
			null to "createdAt DESC"
		DomainAlbumListType.Random ->
			null to "RANDOM()"
		DomainAlbumListType.Downloaded,
		DomainAlbumListType.Recent ->
			null to "lastPlayedAt DESC"
		DomainAlbumListType.Starred ->
			"starredAt IS NOT NULL" to "starredAt ASC"
		is DomainAlbumListType.ByGenre ->
			"genre = ?" to "LOWER(name) ASC"
		is DomainAlbumListType.ByYear ->
			"COALESCE(year, 0) BETWEEN ? AND ?" to "LOWER(name) ASC"
	}

	val sql = buildString {
		append("SELECT * FROM AlbumEntity")
		if (where != null) append(" WHERE $where")
		append(" ORDER BY $orderBy")
	}

	return RoomRawQuery(sql) { statement ->
		when (this) {
			is DomainAlbumListType.ByGenre ->
				statement.bindText(1, genre)
			is DomainAlbumListType.ByYear -> {
				statement.bindInt(1, fromYear)
				statement.bindInt(2, toYear)
			}
			else -> Unit
		}
	}
}

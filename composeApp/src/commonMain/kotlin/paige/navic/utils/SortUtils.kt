package paige.navic.utils

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import paige.navic.data.database.dao.DownloadDao
import paige.navic.data.database.entities.DownloadStatus
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainSong
import paige.navic.domain.models.DomainSongListType

suspend fun ImmutableList<DomainSong>.sortedByListType(
	listType: DomainSongListType,
	downloadDao: DownloadDao
): ImmutableList<DomainSong> {
	return when (listType) {
		DomainSongListType.FrequentlyPlayed -> sortedByDescending { it.playCount }
		DomainSongListType.Starred -> filter { it.starredAt != null }.sortedBy { it.starredAt }
		DomainSongListType.Random -> shuffled()
		DomainSongListType.Downloaded -> filter { song ->
			downloadDao.getAllDownloadsList()
				.filter { it.status == DownloadStatus.DOWNLOADED }
				.any { it.songId == song.id }
		}
		DomainSongListType.Rating -> sortedByDescending { it.userRating ?: 0 }
	}.toImmutableList()
}

suspend fun ImmutableList<DomainAlbum>.sortedByListType(
	listType: DomainAlbumListType,
	downloadDao: DownloadDao
): ImmutableList<DomainAlbum> {
	return when (listType) {
		DomainAlbumListType.AlphabeticalByArtist -> this.sortedBy { it.artistName.lowercase() }
		DomainAlbumListType.AlphabeticalByName -> this.sortedBy { it.name.lowercase() }
		DomainAlbumListType.Frequent -> this.filter { it.playCount != 0 }
			.sortedByDescending { it.playCount }

		DomainAlbumListType.Highest -> this.sortedByDescending { it.userRating }
		DomainAlbumListType.Newest -> this.sortedByDescending { it.createdAt }
		DomainAlbumListType.Random -> this.shuffled()
		DomainAlbumListType.Recent -> this.sortedByDescending { it.lastPlayedAt }
		DomainAlbumListType.Starred -> this.filter { it.starredAt != null }
			.sortedBy { it.starredAt }

		is DomainAlbumListType.ByGenre -> this.filter { it.genre == listType.genre }
		is DomainAlbumListType.ByYear -> this.filter {
			(it.year ?: 0) >= listType.fromYear
				&& (it.year ?: 0) <= listType.toYear
		}

		DomainAlbumListType.Downloaded -> filter { album ->
			downloadDao.getAllDownloadsList()
				.filter { it.status == DownloadStatus.DOWNLOADED }
				.map { it.songId }
				.containsAll(album.songs.map { it.id })
		}
	}.toImmutableList()
}

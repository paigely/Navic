package paige.navic.utils

import paige.navic.data.models.settings.enums.PlaylistSortMode
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumListType
import paige.navic.domain.models.DomainPlaylist

fun List<DomainPlaylist>.sortedByMode(mode: PlaylistSortMode, reversed: Boolean): List<DomainPlaylist> {
	val playlists = when (mode) {
		PlaylistSortMode.Name -> sortedBy { it.name.lowercase() }
		PlaylistSortMode.DateAdded -> sortedBy { it.createdAt }
		PlaylistSortMode.Duration -> sortedBy { it.duration }
	}
	return if (reversed) playlists.reversed() else playlists
}

fun List<DomainAlbum>.sortedByListType(listType: DomainAlbumListType): List<DomainAlbum> {
	return when (listType) {
		DomainAlbumListType.AlphabeticalByArtist -> this.sortedBy { it.artistName.lowercase() }
		DomainAlbumListType.AlphabeticalByName -> this.sortedBy { it.name.lowercase() }
		DomainAlbumListType.Frequent -> this.filter { it.playCount != 0 }.sortedByDescending { it.playCount }
		DomainAlbumListType.Highest -> this.sortedByDescending { it.userRating }
		DomainAlbumListType.Newest -> this.sortedByDescending { it.createdAt }
		DomainAlbumListType.Random -> this.shuffled()
		DomainAlbumListType.Recent -> this.sortedByDescending { it.lastPlayedAt }
		DomainAlbumListType.Starred -> this.filter { it.starredAt != null }.sortedBy { it.starredAt }
		is DomainAlbumListType.ByGenre -> this.filter { it.genre == listType.genre }
		is DomainAlbumListType.ByYear -> this.filter {
			(it.year ?: 0) >= listType.fromYear
				&& (it.year ?: 0) <= listType.toYear
		}
	}
}

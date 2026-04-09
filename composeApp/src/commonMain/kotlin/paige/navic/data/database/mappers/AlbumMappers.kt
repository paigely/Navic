package paige.navic.data.database.mappers

import paige.navic.data.database.entities.AlbumEntity
import paige.navic.data.database.relations.AlbumWithSongs
import paige.navic.domain.models.DomainAlbum
import paige.navic.domain.models.DomainAlbumInfo
import dev.zt64.subsonic.api.model.Album as ApiAlbum
import dev.zt64.subsonic.api.model.AlbumInfo as ApiAlbumInfo

fun ApiAlbum.toEntity() = AlbumEntity(
	albumId = this.id,
	name = this.name,
	artistId = this.artistId,
	artistName = this.artistName,
	coverArtId = this.coverArtId,
	songCount = this.songCount,
	duration = this.duration,
	year = this.year,
	genre = this.genre,
	starredAt = this.starredAt,
	userRating = this.userRating,
	musicBrainzId = this.musicBrainzId,
	createdAt = this.createdAt,
	lastPlayedAt = this.lastPlayedAt,
	playCount = this.playCount,
	genres = this.genres,
	version = this.version
)

fun AlbumWithSongs.toDomainModel() = DomainAlbum(
	id = album.albumId,
	name = album.name,
	artistId = album.artistId,
	artistName = album.artistName,
	coverArtId = album.coverArtId,
	songCount = album.songCount,
	duration = album.duration,
	year = album.year,
	genre = album.genre,
	starredAt = album.starredAt,
	userRating = album.userRating,
	musicBrainzId = album.musicBrainzId,
	createdAt = album.createdAt,
	lastPlayedAt = album.lastPlayedAt,
	playCount = album.playCount,
	genres = album.genres,
	version = album.version,
	songs = songs
		.map { it.toDomainModel() }
		.sortedBy { it.trackNumber }
)

fun DomainAlbum.toEntity() = AlbumEntity(
	albumId = this.id,
	name = this.name,
	artistId = this.artistId,
	artistName = this.artistName,
	coverArtId = this.coverArtId,
	songCount = this.songCount,
	duration = this.duration,
	year = this.year,
	genre = this.genre,
	starredAt = this.starredAt,
	userRating = this.userRating,
	musicBrainzId = this.musicBrainzId,
	createdAt = this.createdAt,
	lastPlayedAt = this.lastPlayedAt,
	playCount = this.playCount,
	genres = this.genres,
	version = this.version
)

fun ApiAlbumInfo.toDomainModel() = DomainAlbumInfo(
	musicBrainzId = musicBrainzId,
	largeImageUrl = largeImageUrl,
	mediumImageUrl = mediumImageUrl,
	smallImageUrl = smallImageUrl,
	lastFmUrl = lastFmUrl,
	notes = notes
)

package paige.navic.data.database.mappers

import paige.navic.data.database.entities.PlaylistEntity
import paige.navic.data.database.relations.PlaylistWithSongs
import paige.navic.domain.models.DomainPlaylist
import dev.zt64.subsonic.api.model.Playlist as ApiPlaylist

fun ApiPlaylist.toEntity() = PlaylistEntity(
	playlistId = id,
	name = name,
	owner = owner,
	comment = comment,
	coverArtId = coverArtId,
	songCount = songCount,
	duration = duration,
	createdAt = createdAt,
	modifiedAt = modifiedAt,
	public = public,
	readOnly = readOnly,
	allowedUsers = allowedUsers,
	validUntil = validUntil
)

fun PlaylistWithSongs.toDomainModel() = DomainPlaylist(
	id = playlist.playlistId,
	name = playlist.name,
	owner = playlist.owner,
	comment = playlist.comment,
	coverArtId = playlist.coverArtId,
	songCount = playlist.songCount,
	duration = playlist.duration,
	createdAt = playlist.createdAt,
	modifiedAt = playlist.modifiedAt,
	public = playlist.public,
	readOnly = playlist.readOnly,
	allowedUsers = playlist.allowedUsers,
	validUntil = playlist.validUntil,
	songs = songs
		.sortedBy { it.crossRef.position }
		.map { it.song.toDomainModel() }
)

fun DomainPlaylist.toEntity() = PlaylistEntity(
	playlistId = id,
	name = name,
	owner = owner,
	comment = comment,
	coverArtId = coverArtId,
	songCount = songCount,
	duration = duration,
	createdAt = createdAt,
	modifiedAt = modifiedAt,
	public = public,
	readOnly = readOnly,
	allowedUsers = allowedUsers,
	validUntil = validUntil
)

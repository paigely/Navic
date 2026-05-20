package paige.navic.data.database.mappers

import paige.navic.data.database.entities.GenreEntity
import paige.navic.data.database.relations.GenreWithAlbums
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainGenre
import dev.zt64.subsonic.api.model.Genre as ApiGenre

fun ApiGenre.toEntity(serverId: String = SessionManager.activeServerId.value ?: "") = GenreEntity(
	genreName = name,
	serverId = serverId,
	albumCount = albumCount,
	songCount = songCount
)

fun GenreWithAlbums.toDomainModel() = DomainGenre(
	name = genre.genreName,
	albumCount = genre.albumCount,
	songCount = genre.songCount,
	albums = albums.map { it.toDomainModel() }
)

fun DomainGenre.toEntity(serverId: String = SessionManager.activeServerId.value ?: "") = GenreEntity(
	genreName = name,
	serverId = serverId,
	albumCount = albumCount,
	songCount = songCount
)

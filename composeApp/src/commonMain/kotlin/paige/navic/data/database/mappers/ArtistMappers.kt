package paige.navic.data.database.mappers

import paige.navic.data.database.entities.ArtistEntity
import paige.navic.data.session.SessionManager
import paige.navic.domain.models.DomainArtist
import dev.zt64.subsonic.api.model.Artist as ApiArtist

fun ApiArtist.toEntity(serverId: String = SessionManager.activeServerId.value ?: "") = ArtistEntity(
	artistId = this.id,
	serverId = serverId,
	name = this.name,
	albumCount = this.albumCount,
	coverArtId = this.coverArtId,
	artistImageUrl = this.artistImageUrl,
	starredAt = this.starredAt,
	userRating = this.userRating,
	sortName = this.sortName,
	musicBrainzId = this.musicBrainzId,
	lastFmUrl = null,
	roles = this.roles,
	biography = null,
	similarArtistIds = emptyList()
)

fun ArtistEntity.toDomainModel() = DomainArtist(
	id = this.artistId,
	name = this.name,
	albumCount = this.albumCount,
	coverArtId = this.coverArtId,
	artistImageUrl = this.artistImageUrl,
	starredAt = this.starredAt,
	userRating = this.userRating,
	sortName = this.sortName,
	musicBrainzId = this.musicBrainzId,
	lastFmUrl = this.lastFmUrl,
	roles = this.roles,
	biography = this.biography,
	similarArtistIds = this.similarArtistIds
)

fun DomainArtist.toEntity(serverId: String = SessionManager.activeServerId.value ?: "") = ArtistEntity(
	artistId = this.id,
	serverId = serverId,
	name = this.name,
	albumCount = this.albumCount,
	coverArtId = this.coverArtId,
	artistImageUrl = this.artistImageUrl,
	starredAt = this.starredAt,
	userRating = this.userRating,
	sortName = this.sortName,
	musicBrainzId = this.musicBrainzId,
	lastFmUrl = this.lastFmUrl,
	roles = this.roles,
	biography = this.biography,
	similarArtistIds = this.similarArtistIds
)

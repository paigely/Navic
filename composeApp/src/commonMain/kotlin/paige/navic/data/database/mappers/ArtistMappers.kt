package paige.navic.data.database.mappers

import dev.zt64.subsonic.api.model.Artist as ApiArtist
import paige.navic.data.database.entities.ArtistEntity
import paige.navic.domain.models.DomainArtist

fun ApiArtist.toEntity() = ArtistEntity(
	artistId = this.id,
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

fun DomainArtist.toEntity() = ArtistEntity(
	artistId = this.id,
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
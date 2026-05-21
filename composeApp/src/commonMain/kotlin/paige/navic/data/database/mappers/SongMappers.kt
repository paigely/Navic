package paige.navic.data.database.mappers

import paige.navic.data.database.entities.SongEntity
import paige.navic.domain.models.DomainContributor
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.DomainSong
import kotlin.time.Duration.Companion.seconds
import dev.zt64.subsonic.api.model.Song as ApiSong

fun ApiSong.toEntity() = SongEntity(
	songId = this.id,
	title = this.title,
	artistName = this.artistName,
	// TODO: figure out why this can be null and how to handle it
	artistId = this.artistId ?: "unknown artist",
	albumTitle = this.albumTitle,
	belongsToAlbumId = this.albumId,
	coverArtId = this.coverArtId,
	duration = this.duration ?: 0.seconds,
	trackNumber = this.trackNumber,
	discNumber = this.discNumber,
	year = this.year,
	genre = this.genre,
	bitRate = this.bitRate,
	mimeType = this.mimeType,
	fileExtension = this.fileExtension,
	filePath = this.filePath,
	starredAt = this.starredAt,
	parentId = this.parentId,
	genres = this.genres,
	moods = this.moods,
	isrc = this.isrc,
	bpm = this.bpm,
	comment = this.comment,
	playCount = this.playCount,
	userRating = this.userRating,
	averageRating = this.averageRating,
	bitDepth = this.bitDepth,
	sampleRate = this.sampleRate,
	audioChannelCount = this.audioChannelCount,
	fileSize = this.fileSize ?: 0L,
	musicBrainzId = this.musicBrainzId,
	contributors = this.contributors.map {
		DomainContributor(
			role = it.role,
			subRole = it.subRole,
			artistId = it.artist.id,
			artistName = it.artist.name
		)
	},
	replayGain = this.replayGain?.let {
		DomainReplayGain(
			albumGain = it.albumGain,
			albumPeak = it.albumPeak,
			trackGain = it.trackGain,
			trackPeak = it.trackPeak,
			baseGain = it.baseGain,
			fallbackGain = it.fallbackGain
		)
	},
	explicitStatus = when (this.explicitStatus) {
		ApiSong.ExplicitStatus.EXPLICIT -> DomainExplicitStatus.Explicit
		ApiSong.ExplicitStatus.CLEAN -> DomainExplicitStatus.Clean
		else -> DomainExplicitStatus.Unknown
	}
)

fun SongEntity.toDomainModel() = DomainSong(
	id = this.songId,
	title = this.title,
	artistName = this.artistName,
	artistId = this.artistId,
	albumTitle = this.albumTitle,
	albumId = this.belongsToAlbumId,
	coverArtId = this.coverArtId,
	duration = this.duration,
	trackNumber = this.trackNumber,
	discNumber = this.discNumber,
	year = this.year,
	genre = this.genre,
	bitRate = this.bitRate,
	mimeType = this.mimeType,
	fileExtension = this.fileExtension,
	filePath = this.filePath,
	starredAt = this.starredAt,
	parentId = this.parentId,
	genres = this.genres,
	moods = this.moods,
	isrc = this.isrc,
	bpm = this.bpm,
	comment = this.comment,
	playCount = this.playCount,
	userRating = this.userRating,
	averageRating = this.averageRating,
	bitDepth = this.bitDepth,
	sampleRate = this.sampleRate,
	audioChannelCount = this.audioChannelCount,
	fileSize = this.fileSize,
	musicBrainzId = this.musicBrainzId,
	contributors = this.contributors,
	replayGain = this.replayGain,
	explicitStatus = this.explicitStatus
)

fun DomainSong.toEntity() = SongEntity(
	songId = this.id,
	title = this.title,
	artistName = this.artistName,
	artistId = this.artistId,
	albumTitle = this.albumTitle,
	belongsToAlbumId = this.albumId,
	coverArtId = this.coverArtId,
	duration = this.duration,
	trackNumber = this.trackNumber,
	discNumber = this.discNumber,
	year = this.year,
	genre = this.genre,
	bitRate = this.bitRate,
	mimeType = this.mimeType,
	fileExtension = this.fileExtension,
	filePath = this.filePath,
	starredAt = this.starredAt,
	parentId = this.parentId,
	genres = this.genres,
	moods = this.moods,
	isrc = this.isrc,
	bpm = this.bpm,
	comment = this.comment,
	playCount = this.playCount,
	userRating = this.userRating,
	averageRating = this.averageRating,
	bitDepth = this.bitDepth,
	sampleRate = this.sampleRate,
	audioChannelCount = this.audioChannelCount,
	fileSize = this.fileSize,
	musicBrainzId = this.musicBrainzId,
	replayGain = this.replayGain,
	contributors = this.contributors,
	explicitStatus = this.explicitStatus
)

package paige.navic.data.database.entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.serialization.Serializable
import paige.navic.domain.models.DomainContributor
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainReplayGain
import kotlin.time.Duration
import kotlin.time.Instant

@Serializable
@Entity
data class SongEntity(
	@PrimaryKey val songId: String,
	val title: String,
	val artistName: String,
	val artistId: String,
	val albumTitle: String?,
	val belongsToAlbumId: String?,
	val parentId: String?,
	val comment: String?,
	val trackNumber: Int?,
	val discNumber: Int?,
	val isrc: List<String>,
	val year: Int?,
	val genre: String?,
	val genres: List<String>,
	val moods: List<String>,
	val duration: Duration,
	val bpm: Int?,
	val contributors: List<DomainContributor>,
	val playCount: Int = 0,
	val userRating: Int?,
	val averageRating: Float?,
	val bitRate: Int?,
	val bitDepth: Int?,
	val sampleRate: Int?,
	val audioChannelCount: Int?,
	val replayGain: DomainReplayGain?,
	val fileSize: Long,
	val fileExtension: String,
	val mimeType: String,
	val filePath: String?,
	val starredAt: Instant?,
	val coverArtId: String?,
	val musicBrainzId: String?,
	val explicitStatus: DomainExplicitStatus
)

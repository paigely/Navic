package paige.navic.domain.models

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Instant

@Immutable
@Serializable
data class DomainSong(
	val id: String,
	val title: String,
	val artistName: String,
	val artistId: String,
	val albumTitle: String?,
	val albumId: String?,
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

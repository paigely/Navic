package paige.navic.data.database

import androidx.room3.ColumnTypeConverter
import paige.navic.domain.models.DomainContributor
import paige.navic.domain.models.DomainExplicitStatus
import paige.navic.domain.models.DomainReplayGain
import paige.navic.domain.models.lyrics.LyricsProvider
import paige.navic.util.core.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

@Suppress("unused")
class Converters {
	// Duration
	@ColumnTypeConverter
	fun fromDuration(duration: Duration?): Long? {
		return duration?.inWholeMilliseconds
	}

	@ColumnTypeConverter
	fun toDuration(millis: Long?): Duration? {
		return millis?.milliseconds
	}

	// Instant
	@ColumnTypeConverter
	fun fromInstant(instant: Instant?): Long? {
		return instant?.toEpochMilliseconds()
	}

	@ColumnTypeConverter
	fun toInstant(millis: Long?): Instant? {
		return millis?.let { Instant.fromEpochMilliseconds(it) }
	}

	// List<String>
	@ColumnTypeConverter
	fun fromStringList(list: List<String>?): String? {
		return list?.joinToString(separator = "||")
	}

	@ColumnTypeConverter
	fun toStringList(data: String?): List<String>? {
		return data?.split("||")?.filter { it.isNotEmpty() }
	}

	// List<Contributor>
	@ColumnTypeConverter
	fun fromContributorList(list: List<DomainContributor>?): String? {
		return list?.joinToString(separator = ";") { c ->
			"${c.role}^${c.subRole ?: ""}^${c.artistId}^${c.artistName}"
		}
	}

	@ColumnTypeConverter
	fun toContributorList(data: String?): List<DomainContributor>? {
		if (data.isNullOrEmpty()) return if (data == null) null else emptyList()

		return data.split(";").filter { it.isNotEmpty() }.map { item ->
			val parts = item.split("^")
			DomainContributor(
				role = parts.getOrNull(0)?.ifEmpty { null } ?: "",
				subRole = parts.getOrNull(1)?.ifEmpty { null },
				artistId = parts.getOrNull(2)?.ifEmpty { null } ?: "",
				artistName = parts.getOrNull(3)?.ifEmpty { null } ?: ""
			)
		}
	}

	// ReplayGain
	@ColumnTypeConverter
	fun fromReplayGain(rg: DomainReplayGain?): String? {
		if (rg == null) return null
		return "${rg.albumGain ?: ""},${rg.albumPeak ?: ""},${rg.trackGain ?: ""},${rg.trackPeak ?: ""},${rg.baseGain ?: ""},${rg.fallbackGain ?: ""}"
	}

	@ColumnTypeConverter
	fun toReplayGain(data: String?): DomainReplayGain? {
		if (data.isNullOrEmpty()) return null
		val parts = data.split(",")
		if (parts.size < 6) return null

		return DomainReplayGain(
			albumGain = parts[0].toFloatOrNull(),
			albumPeak = parts[1].toFloatOrNull(),
			trackGain = parts[2].toFloatOrNull(),
			trackPeak = parts[3].toFloatOrNull(),
			baseGain = parts[4].toFloatOrNull(),
			fallbackGain = parts[5].toFloatOrNull()
		)
	}

	//Lyrics
	@ColumnTypeConverter
	fun fromLyricsProvider(provider: LyricsProvider): String {
		return provider.name
	}

	@ColumnTypeConverter
	fun toLyricsProvider(name: String): LyricsProvider {
		return try {
			LyricsProvider.valueOf(name)
		} catch (e: Exception) {
			Logger.w("Converters", "Unknown lyrics provider", e)
			LyricsProvider.SUBSONIC
		}
	}

	// DomainExplicitStatus
	@ColumnTypeConverter
	fun fromExplicitStatus(explicitStatus: DomainExplicitStatus) = explicitStatus.ordinal

	@ColumnTypeConverter
	fun toExplicitStatus(ordinal: Int) =
		DomainExplicitStatus.entries.getOrNull(ordinal) ?: DomainExplicitStatus.Unknown
}

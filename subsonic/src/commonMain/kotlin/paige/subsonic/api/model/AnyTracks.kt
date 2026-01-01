package paige.subsonic.api.model

import kotlinx.serialization.Serializable

@Serializable
data class AnyTracks(
	val id: String,
	val title: String?,
	val subtitle: String?,
	val coverArt: String?,
	val owner: String?,
	val duration: Int?,
	val trackCount: Int?,
	val tracks: List<AnyTrack>
)

@Serializable
data class AnyTrack(
	val id: String,
	val title: String,
	val artist: String?,
	val album: String?,
	val duration: Int?,
	val trackNumber: Int?,
	val coverArt: String?
)

fun Playlist.toAny(): AnyTracks =
	AnyTracks(
		id = id,
		title = name,
		subtitle = comment,
		coverArt = coverArt,
		owner = owner,
		duration = duration,
		trackCount = songCount,
		tracks = entry.orEmpty().map { it.toAny() }
	)

fun PlaylistEntry.toAny(): AnyTrack =
	AnyTrack(
		id = id,
		title = title,
		artist = artist,
		album = album,
		duration = duration,
		trackNumber = track,
		coverArt = coverArt
	)

fun Album.toAny(): AnyTracks =
	AnyTracks(
		id = id,
		title = name,
		subtitle = artist,
		coverArt = coverArt,
		owner = null,
		duration = duration,
		trackCount = songCount,
		tracks = song.orEmpty().map { it.toAny() }
	)

fun Song.toAny(): AnyTrack =
	AnyTrack(
		id = id,
		title = title,
		artist = artist,
		album = album,
		duration = duration,
		trackNumber = track,
		coverArt = coverArt
	)

package paige.subsonic.api.model

import kotlinx.serialization.Serializable

/**
 * Generic representation of an album or playlist.
 */
@Serializable
sealed interface TrackCollection {
	val id: String
	val title: String?
	val subtitle: String?
	val coverArt: String?
	val duration: Int?
	val year: Int?
	val genre: String?
	val trackCount: Int
	val tracks: List<Track>
	val artistId: String?
}

/**
 * Generic representation of a song or playlist entry.
 */
@Serializable
sealed interface Track {
	val album: String?
	val albumId: String?
	val artist: String?
	val artistId: String?
	val bitRate: Int?
	val bitDepth: Int?
	val channelCount: Int?
	val contentType: String?
	val coverArt: String?
	val created: String?
	val duration: Int?
	val discNumber: Int?
	val genre: String?
	val id: String
	val isDir: Boolean?
	val isVideo: Boolean?
	val parent: String?
	val path: String?
	val playCount: Int?
	val samplingRate: Int?
	val size: Int?
	val suffix: String?
	val title: String
	val track: Int?
	val type: String?
	val year: Int?
	val starred: String?
	val userRating: Int?
}

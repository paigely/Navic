package paige.subsonic.api.models

import kotlinx.serialization.Serializable

/**
 * Generic representation of an album or playlist.
 */
@Serializable
sealed interface TrackCollection {
	val id: String
	val title: String
	val subtitle: String?
	val coverArt: String?
	val duration: Int?
	val year: Int?
	val genre: String?
	val trackCount: Int?
	val tracks: List<Track>
	val artistId: String?
}

/**
 * A media.
 *
 * @property id	The id of the media
 * @property parent	The id of the parent (folder/album)
 * @property isDir	The media is a directory
 * @property title	The media name.
 * @property album	The album name.
 * @property artist	The artist name.
 * @property track	The track number.
 * @property year	The media year.
 * @property genre	The media genre
 * @property coverArt	A covertArt id.
 * @property size	A file size of the media.
 * @property contentType	The mimeType of the media.
 * @property suffix	The file suffix of the media.
 * @property transcodedContentType	The transcoded mediaType if transcoding should happen.
 * @property transcodedSuffix	The file suffix of the transcoded media.
 * @property duration	The duration of the media in seconds.
 * @property bitRate	The bitrate of the media.
 * @property bitDepth	The bit depth of the media.
 * @property samplingRate	The sampling rate of the media.
 * @property channelCount	The number of channels of the media.
 * @property path	The full path of the media.
 * @property isVideo	Media is a video
 * @property userRating	The user rating of the media [1-5]
 * @property averageRating	The average rating of the media [1.0-5.0]
 * @property playCount	The play count.
 * @property discNumber	The disc number.
 * @property created	Date the media was created. [ISO 8601]
 * @property starred	Date the media was starred. [ISO 8601]
 * @property albumId	The corresponding album id
 * @property artistId	The corresponding artist id
 * @property type	The generic type of media [music/podcast/audiobook/video]
 * @property mediaType	The actual media type [song/album/artist] Note: If you support musicBrainzId you must support this field to ensure clients knows what the ID refers to.
 * @property bookmarkPosition	The bookmark position in seconds
 * @property originalWidth	The video original Width
 * @property originalHeight	The video original Height
 * @property played	Date the album was last played. [ISO 8601]
 * @property bpm	The BPM of the song.
 * @property comment	The comment tag of the song.
 * @property sortName	The song sort name.
 * @property musicBrainzId	The track MusicBrainzID.
 * @property isrc	The track ISRC(s).
 * @property genres	The list of all genres of the song.
 * @property artists	The list of all song artists of the song. (Note: Only the required ArtistID3 fields should be returned by default)
 * @property displayArtist	The single value display artist.
 * @property albumArtists	The list of all album artists of the song. (Note: Only the required ArtistID3 fields should be returned by default)
 * @property displayAlbumArtist	The single value display album artist.
 * @property contributors	The list of all contributor artists of the song.
 * @property displayComposer	The single value display composer.
 * @property moods	The list of all moods of the song.
 * @property replayGain	The replaygain data of the song.
 * @property explicitStatus	Returns “explicit”, “clean” or “”. (For songs extracted from tags “ITUNESADVISORY”: 1 = explicit, 2 = clean, MP4 “rtng”: 1 or 4 = explicit, 2 = clean. See albumID3 for albums)
 */
@Serializable
data class Track(
	val id: String,
	val parent: String?,
	val isDir: Boolean,
	val title: String,
	val album: String?,
	val artist: String?,
	val track: Int?,
	val year: Int?,
	val genre: String?,
	val coverArt: String?,
	val size: Long?,
	val contentType: String?,
	val suffix: String?,
	val transcodedContentType: String?,
	val transcodedSuffix: String?,
	val duration: Int?,
	val bitRate: Int?,
	val bitDepth: Int?,
	val samplingRate: Int?,
	val channelCount: Int?,
	val path: String?,
	val isVideo: Boolean?,
	val userRating: Int?,
	val averageRating: Float?,
	val playCount: Long?,
	val discNumber: Int?,
	val created: String?,
	val starred: String?,
	val albumId: String?,
	val artistId: String?,
	val type: String?,
	val mediaType: String?,
	val bookmarkPosition: Long?,
	val originalWidth: Int?,
	val originalHeight: Int?,
	val played: String?,
	val bpm: Int?,
	val comment: String?,
	val sortName: String?,
	val musicBrainzId: String?,
	val isrc: List<String>?,
	val genres: List<ItemGenre>?,
	val artists: List<Artist>?,
	val displayArtist: String,
	val albumArtists: List<Artist>?,
	val displayAlbumArtist: String,
	val contributors: List<Contributor>?,
	val displayComposer: String,
	val moods: List<String>?,
	val replayGain: ReplayGain?,
	val explicitStatus: String?
)

/**
 * The replay gain data of a song.
 *
 * @property trackGain The track replay gain value. (In Db)
 * @property albumGain The album replay gain value. (In Db)
 * @property trackPeak The track peak value. (Must be positive)
 * @property albumPeak The album peak value. (Must be positive)
 * @property baseGain The base gain value. (In Db) (Ogg Opus Output Gain for example)
 * @property fallbackGain An optional fallback gain that clients should apply when the corresponding gain value is missing. (Can be computed from the tracks or exposed as an user setting.)
 */
@Serializable
data class ReplayGain(
	val trackGain: Float?,
	val albumGain: Float?,
	val trackPeak: Float?,
	val albumPeak: Float?,
	val baseGain: Float?,
	val fallbackGain: Float?
)

/**
 * A genre returned in list of genres for an item.
 *
 * @property name Genre name
 */
@Serializable
data class ItemGenre(
	val name: String?
)

/**
 * A contributor artist for a song or an album.
 *
 * @property role The contributor role.
 * @property subRole The subRole for roles that may require it. Ex: The instrument for the performer role (TMCL/performer tags). Note: For consistency between different tag formats, the TIPL sub roles should be directly exposed in the role field.
 * @property artist The artist taking on the role. (Note: Only the required ArtistID3 fields should be returned by default)
 */
@Serializable
data class Contributor(
	val role: String,
	val subRole: String?,
	val artist: Artist
)

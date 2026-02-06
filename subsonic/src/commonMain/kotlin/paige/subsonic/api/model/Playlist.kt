package paige.subsonic.api.model

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistResponse(
	val playlist: Playlist
)

@Serializable
data class PlaylistsResponse(
	val playlists: PlaylistsData
) {
	@Serializable
	data class PlaylistsData(
		val playlist: List<Playlist>?
	)
}

@Serializable
data class Playlist(
	override val coverArt: String?,
	override val duration: Int,
	override val id: String,
	val changed: String?,
	val created: String,
	val entry: List<PlaylistEntry>?,
	val name: String,
	val comment: String?,
	val owner: String,
	val `public`: Boolean?,
	val songCount: Int,
) : TrackCollection {
	override val title: String = name
	override val subtitle: String? = comment
	override val tracks: List<Track> = entry.orEmpty()
	override val trackCount: Int = songCount
	override val genre: String? = null
	override val year: Int? = null
	override val artistId: String? = null
}

@Serializable
data class PlaylistEntry(
	override val album: String?,
	override val albumId: String?,
	override val artist: String?,
	override val artistId: String?,
	override val bitRate: Int?,
	override val contentType: String?,
	override val coverArt: String?,
	override val created: String?,
	override val discNumber: Int?,
	override val duration: Int?,
	override val genre: String?,
	override val id: String,
	override val isDir: Boolean?,
	override val isVideo: Boolean?,
	override val parent: String?,
	override val path: String?,
	override val playCount: Int?,
	override val size: Int?,
	override val suffix: String?,
	override val starred: String?,
	override val userRating: Int?,
	override val title: String,
	override val track: Int?,
	val transcodedContentType: String?,
	val transcodedSuffix: String?,
	override val type: String?,
	override val year: Int?,
	override val bitDepth: Int?,
	override val channelCount: Int?,
	override val samplingRate: Int?,
) : Track

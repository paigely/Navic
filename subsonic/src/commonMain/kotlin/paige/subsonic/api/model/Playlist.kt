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
	val changed: String?,
	val coverArt: String?,
	val created: String,
	val duration: Int,
	val entry: List<PlaylistEntry>?,
	val id: String,
	val name: String,
	val comment: String?,
	val owner: String,
	val `public`: Boolean?,
	val songCount: Int,
)

@Serializable
data class PlaylistEntry(
	val album: String?,
	val albumId: String?,
	val artist: String?,
	val artistId: String?,
	val bitRate: Int?,
	val contentType: String?,
	val coverArt: String?,
	val created: String?,
	val discNumber: Int?,
	val duration: Int?,
	val genre: String?,
	val id: String,
	val isDir: Boolean?,
	val isVideo: Boolean?,
	val parent: String?,
	val path: String?,
	val playCount: Int?,
	val size: Int?,
	val suffix: String?,
	val starred: String?,
	val userRating: Int?,
	val title: String,
	val track: Int?,
	val transcodedContentType: String?,
	val transcodedSuffix: String?,
	val type: String?,
	val year: Int?,
)

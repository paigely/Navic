package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistResponse(
	val playlist: Playlist
)

@Serializable
data class CreatePlaylistResponse(
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
	val entry: List<Track>?,
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

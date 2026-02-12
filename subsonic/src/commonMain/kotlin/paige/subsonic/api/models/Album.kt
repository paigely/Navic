package paige.subsonic.api.models

import kotlinx.serialization.Serializable

@Serializable
data class AlbumResponse(
	val album: Album
)

@Serializable
data class AlbumListResponse(
	val albumList: AlbumList
)

@Serializable
data class AlbumList2Response(
	val albumList2: AlbumList
)

@Serializable
data class AlbumList(
	val album: List<Album>?
)

@Serializable
data class AlbumInfoResponse(
	val albumInfo: AlbumInfo
)

@Serializable
data class AlbumInfo(
	val notes: String?,
	val musicBrainzId: String?,
	val lastFmUrl: String?,
	val smallImageUrl: String?,
	val mediumImageUrl: String?,
	val largeImageUrl: String?,
)

@Serializable
data class Album(
	override val coverArt: String?,
	override val duration: Int?,
	override val id: String,
	override val year: Int?,
	override val genre: String?,
	override val artistId: String?,
	val artist: String?,
	val created: String,
	val name: String,
	val playCount: Int?,
	val song: List<Track>?,
	val songCount: Int?,
	val userRating: Int?
) : TrackCollection {
	override val title: String = name
	override val subtitle: String? = artist
	override val tracks: List<Track> = song.orEmpty()
	override val trackCount: Int? = songCount
}

@Serializable
data class SearchResult3Response(
	val searchResult3: SearchResult3
)

@Serializable
data class SearchResult3(
	val song: List<Track>?,
	val album: List<Album>?,
	val artist: List<Artist>?
)

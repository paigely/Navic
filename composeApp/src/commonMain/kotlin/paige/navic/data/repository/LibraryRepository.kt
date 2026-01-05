package paige.navic.data.repository

import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.ListType

class LibraryRepository {
	suspend fun getAlbums(): List<Album> {
		return SessionManager.api
			.getAlbumList(type = ListType.ALPHABETICAL_BY_ARTIST, size = 500)
			.data.albumList.album.orEmpty().map { album ->
				album.copy(
					coverArt = SessionManager.api
						.getCoverArtUrl(album.coverArt, auth = true)
				)
			}
	}
	suspend fun isAlbumStarred(album: Album): Boolean? {
		return SessionManager.api.getStarred()
				.data.starred.album
				?.map { it.id }
				?.contains(album.id)
	}
	suspend fun starAlbum(album: Album) {
		SessionManager.api.star(listOf(album.id))
	}
	suspend fun unstarAlbum(album: Album) {
		SessionManager.api.unstar(listOf(album.id))
	}
}

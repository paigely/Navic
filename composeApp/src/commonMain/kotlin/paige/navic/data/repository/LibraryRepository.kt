package paige.navic.data.repository

import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.ListType

class LibraryRepository {
	suspend fun getAlbums(): List<Album> {
		return SessionManager.api
			.getAlbumList(type = ListType.ALPHABETICAL_BY_ARTIST, size = 500)
			.data.albumList.album.orEmpty().map { album ->
				SessionManager.api.getAlbum(album.id).data.album.copy(
					coverArt = SessionManager.api
						.getCoverArtUrl(album.coverArt, auth = true)
				)
			}
	}
}

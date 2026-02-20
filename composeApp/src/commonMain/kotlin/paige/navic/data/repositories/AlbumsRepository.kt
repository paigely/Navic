package paige.navic.data.repositories

import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Album
import paige.subsonic.api.models.ListType

open class AlbumsRepository {
	open suspend fun getAlbums(
		offset: Int = 0,
		listType: ListType = ListType.ALPHABETICAL_BY_ARTIST
	): List<Album> {
		return SessionManager.api
			.getAlbumList(type = listType, size = 30, offset = offset)
			.data.albumList.album.orEmpty()
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

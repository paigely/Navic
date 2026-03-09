package paige.navic.data.repositories

import dev.zt64.subsonic.api.model.Album
import dev.zt64.subsonic.api.model.AlbumListType
import paige.navic.data.session.SessionManager

open class AlbumsRepository {
	open suspend fun getAlbums(
		offset: Int = 0,
		listType: AlbumListType = AlbumListType.AlphabeticalByArtist
	): List<Album> {
		return SessionManager.api
			.getAlbums(type = listType, size = 30, offset = offset)
	}
	suspend fun isAlbumStarred(album: Album): Boolean {
		return album in SessionManager.api.getStarred().albums
	}
	suspend fun starAlbum(album: Album) {
		SessionManager.api.star(album.id)
	}
	suspend fun unstarAlbum(album: Album) {
		SessionManager.api.unstar(album.id)
	}
}
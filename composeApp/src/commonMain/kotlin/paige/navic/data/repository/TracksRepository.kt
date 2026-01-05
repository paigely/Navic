package paige.navic.data.repository

import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.AnyTracks
import paige.subsonic.api.model.Playlist
import paige.subsonic.api.model.toAny

class TracksRepository {
	suspend fun getTracks(album: Album): AnyTracks {
		return SessionManager.api.getAlbum(album.id).data.album.toAny().copy(
			coverArt = SessionManager.api.getCoverArtUrl(album.id, auth = true)
		)
	}
	suspend fun getTracks(playlist: Playlist): AnyTracks {
		return SessionManager.api.getPlaylist(playlist.id).data.playlist.toAny().copy(
			coverArt = SessionManager.api.getCoverArtUrl(playlist.id, auth = true)
		)
	}
}
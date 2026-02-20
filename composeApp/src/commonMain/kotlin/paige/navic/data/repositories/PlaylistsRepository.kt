package paige.navic.data.repositories

import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Playlist

class PlaylistsRepository {
	suspend fun getPlaylists(): List<Playlist> {
		return SessionManager.api
			.getPlaylists()
			.data.playlists.playlist.orEmpty()
	}
}

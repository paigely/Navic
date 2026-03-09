package paige.navic.data.repositories

import dev.zt64.subsonic.api.model.Playlist
import paige.navic.data.session.SessionManager

class PlaylistsRepository {
	suspend fun getPlaylists(): List<Playlist> = SessionManager.api.getPlaylists()
}
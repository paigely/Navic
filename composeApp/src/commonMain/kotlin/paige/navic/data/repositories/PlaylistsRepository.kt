package paige.navic.data.repositories

import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Playlist

class PlaylistsRepository {
	suspend fun getPlaylists(): List<Playlist> {
		return SessionManager.api
			.getPlaylists()
			.data.playlists.playlist.orEmpty().map { playlist ->
				SessionManager.api.getPlaylist(playlist.id).data.playlist.copy(
					coverArt = SessionManager.api
						.getCoverArtUrl(playlist.coverArt, size = 512, auth = true)
				)
			}
	}
}

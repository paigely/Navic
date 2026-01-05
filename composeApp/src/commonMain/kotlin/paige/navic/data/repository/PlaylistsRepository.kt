package paige.navic.data.repository

import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Playlist

class PlaylistsRepository {
	suspend fun getPlaylists(): List<Playlist> {
		return SessionManager.api
			.getPlaylists()
			.data.playlists.playlist.orEmpty().map { playlist ->
				SessionManager.api.getPlaylist(playlist.id).data.playlist.copy(
					coverArt = SessionManager.api
						.getCoverArtUrl(playlist.coverArt, auth = true)
				)
			}
	}
}

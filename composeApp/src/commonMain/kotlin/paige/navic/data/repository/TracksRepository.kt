package paige.navic.data.repository

import paige.navic.data.session.SessionManager
import paige.subsonic.api.model.Album
import paige.subsonic.api.model.AnyTrack
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
	suspend fun isTrackStarred(track: AnyTrack): Boolean? {
		return SessionManager.api.getStarred()
			.data.starred.song
			?.map { it.id }
			?.contains(track.id)
	}
	suspend fun starTrack(track: AnyTrack) {
		SessionManager.api.star(listOf(track.id))
	}
	suspend fun unstarTrack(track: AnyTrack) {
		SessionManager.api.unstar(listOf(track.id))
	}
}
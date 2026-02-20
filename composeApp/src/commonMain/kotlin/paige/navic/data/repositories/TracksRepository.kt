package paige.navic.data.repositories

import paige.navic.data.session.SessionManager
import paige.subsonic.api.models.Album
import paige.subsonic.api.models.AlbumInfo
import paige.subsonic.api.models.Playlist
import paige.subsonic.api.models.Track
import paige.subsonic.api.models.TrackCollection

class TracksRepository {
	suspend fun fetchWithAllTracks(collection: TrackCollection): TrackCollection {
		return when (collection) {
			is Album -> SessionManager.api.getAlbum(collection.id).data.album
			is Playlist -> SessionManager.api.getPlaylist(collection.id).data.playlist
		}
	}

	suspend fun getAlbumInfo(album: Album): AlbumInfo {
		return SessionManager.api.getAlbumInfo(album.id).data.albumInfo
	}

	suspend fun isTrackStarred(track: Track): Boolean? {
		return SessionManager.api.getStarred()
			.data.starred.song
			?.map { it.id }
			?.contains(track.id)
	}

	suspend fun starTrack(track: Track) {
		SessionManager.api.star(listOf(track.id))
	}

	suspend fun unstarTrack(track: Track) {
		SessionManager.api.unstar(listOf(track.id))
	}
}
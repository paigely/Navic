package paige.navic.data.repositories

import dev.zt64.subsonic.api.model.Album
import dev.zt64.subsonic.api.model.AlbumInfo
import dev.zt64.subsonic.api.model.Playlist
import dev.zt64.subsonic.api.model.Song
import dev.zt64.subsonic.api.model.SongCollection
import paige.navic.data.session.SessionManager

class TracksRepository {
	suspend fun fetchWithAllTracks(collection: SongCollection): SongCollection {
		return when (collection) {
			is Album -> SessionManager.api.getAlbum(collection.id)!!
			is Playlist -> SessionManager.api.getPlaylist(collection.id)
		}
	}

	suspend fun getAlbumInfo(album: Album): AlbumInfo {
		return SessionManager.api.getAlbumInfo(album.id)
	}

	suspend fun isTrackStarred(track: Song): Boolean {
		return track in SessionManager.api.getStarred().songs
	}

	suspend fun starTrack(track: Song) {
		SessionManager.api.star(track.id)
	}

	suspend fun unstarTrack(track: Song) {
		SessionManager.api.unstar(track.id)
	}
}